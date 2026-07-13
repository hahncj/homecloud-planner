package com.homecloud.planner.dashboard;

import com.homecloud.planner.backup.BackupCoverageCalculator;
import com.homecloud.planner.backup.BackupPolicy;
import com.homecloud.planner.device.Device;
import com.homecloud.planner.phase.Phase;
import com.homecloud.planner.shopping.PurchaseItem;
import com.homecloud.planner.shopping.PurchaseStatus;
import com.homecloud.planner.task.Task;
import com.homecloud.planner.task.TaskStatus;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * Produces deterministic, rule-based suggestions from the current state of
 * the project — never AI-generated. Each private method is one rule; see
 * the Milestone 6 build plan for the five required examples.
 */
@Component
public class RecommendedActionEngine {

    static final int MAX_PER_CATEGORY = 5;
    static final int MAX_TOTAL = 10;
    static final int WARRANTY_LOOKAHEAD_DAYS = 30;

    private static final List<PurchaseStatus> PENDING_PURCHASE_STATUSES =
            List.of(PurchaseStatus.IDEA, PurchaseStatus.RESEARCHING, PurchaseStatus.PLANNED, PurchaseStatus.ORDERED);

    public List<RecommendedAction> recommend(
            List<Task> tasks,
            Map<UUID, Boolean> blockedFlags,
            Map<UUID, List<UUID>> dependsOnByTask,
            List<Phase> phasesInSequence,
            List<BackupPolicy> backupPolicies,
            BackupCoverageCalculator backupCalculator,
            List<Device> devices,
            List<PurchaseItem> purchaseItems,
            LocalDate today) {
        List<RecommendedAction> actions = new ArrayList<>();
        actions.addAll(blockingTaskActions(tasks, blockedFlags, dependsOnByTask));
        actions.addAll(overdueBackupActions(backupPolicies, backupCalculator));
        actions.addAll(expiringWarrantyActions(devices, purchaseItems, today));
        currentPhaseFocusAction(phasesInSequence, tasks).ifPresent(actions::add);
        actions.addAll(pendingPurchaseActions(phasesInSequence, tasks, blockedFlags, purchaseItems));
        return actions.stream().limit(MAX_TOTAL).toList();
    }

    /** Rule: complete blocking tasks before dependent tasks. */
    private List<RecommendedAction> blockingTaskActions(
            List<Task> tasks, Map<UUID, Boolean> blockedFlags, Map<UUID, List<UUID>> dependsOnByTask) {
        Map<UUID, Task> byId = tasks.stream().collect(java.util.stream.Collectors.toMap(Task::getId, task -> task));
        List<RecommendedAction> actions = new ArrayList<>();
        for (Task task : tasks) {
            if (actions.size() >= MAX_PER_CATEGORY) {
                break;
            }
            if (!Boolean.TRUE.equals(blockedFlags.get(task.getId()))) {
                continue;
            }
            for (UUID dependsOnId : dependsOnByTask.getOrDefault(task.getId(), List.of())) {
                Task prerequisite = byId.get(dependsOnId);
                if (prerequisite != null && prerequisite.isBlockable()) {
                    actions.add(new RecommendedAction(
                            "BLOCKING_TASK",
                            "Finish '" + prerequisite.getTitle() + "' so '" + task.getTitle() + "' can start."));
                    break;
                }
            }
        }
        return actions;
    }

    /** Rule: verify overdue backups. */
    private List<RecommendedAction> overdueBackupActions(List<BackupPolicy> policies, BackupCoverageCalculator calculator) {
        return policies.stream()
                .filter(calculator::isVerificationOverdue)
                .limit(MAX_PER_CATEGORY)
                .map(policy -> new RecommendedAction(
                        "BACKUP_VERIFICATION",
                        "Verify the backup for '" + policy.getDataCategory() + "'"
                                + (policy.getLastVerifiedDate() == null
                                        ? " — it has never been verified."
                                        : ", last verified " + policy.getLastVerifiedDate() + ".")))
                .toList();
    }

    /** Rule: review expiring warranties. */
    private List<RecommendedAction> expiringWarrantyActions(List<Device> devices, List<PurchaseItem> items, LocalDate today) {
        Stream<RecommendedAction> deviceActions = devices.stream()
                .filter(device -> isExpiringSoon(device.getWarrantyExpiration(), today))
                .map(device -> new RecommendedAction(
                        "WARRANTY",
                        "Review the warranty for '" + device.getName() + "', expiring " + device.getWarrantyExpiration() + "."));
        Stream<RecommendedAction> purchaseActions = items.stream()
                .filter(item -> isExpiringSoon(item.getWarrantyExpiration(), today))
                .map(item -> new RecommendedAction(
                        "WARRANTY",
                        "Review the warranty for '" + item.getProductName() + "', expiring " + item.getWarrantyExpiration() + "."));
        return Stream.concat(deviceActions, purchaseActions).limit(MAX_PER_CATEGORY).toList();
    }

    /** Rule: complete current-phase tasks before future-phase tasks. */
    private Optional<RecommendedAction> currentPhaseFocusAction(List<Phase> phasesInSequence, List<Task> tasks) {
        Phase currentPhase = findCurrentPhase(phasesInSequence, tasks);
        if (currentPhase == null) {
            return Optional.empty();
        }
        boolean laterPhaseHasStartedWork = tasks.stream()
                .filter(task -> task.getPhase().getSequence() > currentPhase.getSequence())
                .anyMatch(task -> task.getStatus() == TaskStatus.IN_PROGRESS || task.getStatus() == TaskStatus.COMPLETED);
        if (!laterPhaseHasStartedWork) {
            return Optional.empty();
        }
        return Optional.of(new RecommendedAction(
                "PHASE_FOCUS", "Finish '" + currentPhase.getName() + "' tasks before continuing work in later phases."));
    }

    /** Rule: resolve planned purchases required by ready tasks. */
    private List<RecommendedAction> pendingPurchaseActions(
            List<Phase> phasesInSequence, List<Task> tasks, Map<UUID, Boolean> blockedFlags, List<PurchaseItem> items) {
        List<RecommendedAction> actions = new ArrayList<>();
        for (Phase phase : phasesInSequence) {
            if (actions.size() >= MAX_PER_CATEGORY) {
                break;
            }
            boolean hasReadyTask = tasks.stream()
                    .anyMatch(task -> task.getPhase().getId().equals(phase.getId())
                            && task.getStatus() == TaskStatus.NOT_STARTED
                            && !Boolean.TRUE.equals(blockedFlags.get(task.getId())));
            if (!hasReadyTask) {
                continue;
            }
            boolean hasPendingPurchase = items.stream()
                    .anyMatch(item -> item.getPhase() != null
                            && item.getPhase().getId().equals(phase.getId())
                            && PENDING_PURCHASE_STATUSES.contains(item.getStatus()));
            if (hasPendingPurchase) {
                actions.add(new RecommendedAction(
                        "PENDING_PURCHASE", "Resolve pending purchases in '" + phase.getName() + "' before starting its tasks."));
            }
        }
        return actions;
    }

    private Phase findCurrentPhase(List<Phase> phasesInSequence, List<Task> tasks) {
        return phasesInSequence.stream()
                .sorted(Comparator.comparingInt(Phase::getSequence))
                .filter(phase -> tasks.stream()
                        .anyMatch(task -> task.getPhase().getId().equals(phase.getId()) && task.isEligibleForProgress()
                                && task.getStatus() != TaskStatus.COMPLETED))
                .findFirst()
                .orElse(null);
    }

    private boolean isExpiringSoon(LocalDate expiration, LocalDate today) {
        if (expiration == null) {
            return false;
        }
        long daysUntilExpiration = ChronoUnit.DAYS.between(today, expiration);
        return daysUntilExpiration >= 0 && daysUntilExpiration <= WARRANTY_LOOKAHEAD_DAYS;
    }
}
