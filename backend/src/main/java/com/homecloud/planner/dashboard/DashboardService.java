package com.homecloud.planner.dashboard;

import com.homecloud.planner.backup.BackupCoverageCalculator;
import com.homecloud.planner.backup.BackupPolicy;
import com.homecloud.planner.backup.BackupPolicyRepository;
import com.homecloud.planner.budget.BudgetCalculator;
import com.homecloud.planner.budget.BudgetSummary;
import com.homecloud.planner.common.NotFoundException;
import com.homecloud.planner.device.Device;
import com.homecloud.planner.device.DeviceRepository;
import com.homecloud.planner.phase.Phase;
import com.homecloud.planner.phase.PhaseRepository;
import com.homecloud.planner.project.Project;
import com.homecloud.planner.project.ProjectRepository;
import com.homecloud.planner.roadmap.ProgressSummary;
import com.homecloud.planner.roadmap.TaskProgressCalculator;
import com.homecloud.planner.servicecatalog.ManagedService;
import com.homecloud.planner.servicecatalog.ManagedServiceRepository;
import com.homecloud.planner.shopping.PurchaseItem;
import com.homecloud.planner.shopping.PurchaseItemRepository;
import com.homecloud.planner.task.Task;
import com.homecloud.planner.task.TaskDependency;
import com.homecloud.planner.task.TaskDependencyRepository;
import com.homecloud.planner.task.TaskRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final int LIST_LIMIT = 10;
    private static final int WARRANTY_LOOKAHEAD_DAYS = 30;

    private final ProjectRepository projectRepository;
    private final PhaseRepository phaseRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskProgressCalculator taskProgressCalculator;
    private final PurchaseItemRepository purchaseItemRepository;
    private final BudgetCalculator budgetCalculator;
    private final DeviceRepository deviceRepository;
    private final ManagedServiceRepository managedServiceRepository;
    private final BackupPolicyRepository backupPolicyRepository;
    private final BackupCoverageCalculator backupCoverageCalculator;
    private final RecommendedActionEngine recommendedActionEngine;
    private final Clock clock;

    public DashboardService(
            ProjectRepository projectRepository,
            PhaseRepository phaseRepository,
            TaskRepository taskRepository,
            TaskDependencyRepository taskDependencyRepository,
            TaskProgressCalculator taskProgressCalculator,
            PurchaseItemRepository purchaseItemRepository,
            BudgetCalculator budgetCalculator,
            DeviceRepository deviceRepository,
            ManagedServiceRepository managedServiceRepository,
            BackupPolicyRepository backupPolicyRepository,
            BackupCoverageCalculator backupCoverageCalculator,
            RecommendedActionEngine recommendedActionEngine,
            Clock clock) {
        this.projectRepository = projectRepository;
        this.phaseRepository = phaseRepository;
        this.taskRepository = taskRepository;
        this.taskDependencyRepository = taskDependencyRepository;
        this.taskProgressCalculator = taskProgressCalculator;
        this.purchaseItemRepository = purchaseItemRepository;
        this.budgetCalculator = budgetCalculator;
        this.deviceRepository = deviceRepository;
        this.managedServiceRepository = managedServiceRepository;
        this.backupPolicyRepository = backupPolicyRepository;
        this.backupCoverageCalculator = backupCoverageCalculator;
        this.recommendedActionEngine = recommendedActionEngine;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardSummary getDashboard(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project " + projectId + " was not found."));
        LocalDate today = LocalDate.now(clock);

        List<Phase> phases = phaseRepository.findByProjectIdOrderBySequenceAsc(projectId);
        List<Task> tasks = taskRepository.findByPhaseProjectId(projectId);
        List<TaskDependency> dependencies = taskDependencyRepository.findByTaskPhaseProjectId(projectId);
        Map<UUID, Boolean> blockedFlags = taskProgressCalculator.computeBlockedFlags(tasks, dependencies);
        Map<UUID, List<UUID>> dependsOnByTask = dependencies.stream()
                .collect(Collectors.groupingBy(
                        dependency -> dependency.getTask().getId(),
                        Collectors.mapping(dependency -> dependency.getDependsOnTask().getId(), Collectors.toList())));

        List<PurchaseItem> purchaseItems = purchaseItemRepository.findByProjectId(projectId);
        BudgetSummary budgetSummary = budgetCalculator.summarize(project.getBudget(), purchaseItems);

        List<Device> devices = deviceRepository.findByProjectId(projectId);
        List<ManagedService> services = managedServiceRepository.findByProjectId(projectId);
        List<BackupPolicy> backupPolicies = backupPolicyRepository.findByProjectId(projectId);

        ProgressSummary overallProgress = taskProgressCalculator.summarize(tasks, blockedFlags);
        PhaseSummary currentPhase = findCurrentPhase(phases, tasks, blockedFlags);

        List<TaskSummary> blockedTaskSummaries = tasks.stream()
                .filter(task -> Boolean.TRUE.equals(blockedFlags.get(task.getId())))
                .map(task -> TaskSummary.of(task, true))
                .toList();

        List<TaskSummary> upcomingTargetDates = tasks.stream()
                .filter(task -> task.getTargetDate() != null && task.isEligibleForProgress())
                .filter(task -> !task.getTargetDate().isBefore(today)
                        && !task.getTargetDate().isAfter(today.plusDays(WARRANTY_LOOKAHEAD_DAYS)))
                .sorted(Comparator.comparing(Task::getTargetDate))
                .limit(LIST_LIMIT)
                .map(task -> TaskSummary.of(task, Boolean.TRUE.equals(blockedFlags.get(task.getId()))))
                .toList();

        List<TaskSummary> recentCompletedTasks = tasks.stream()
                .filter(task -> task.getCompletedDate() != null)
                .sorted(Comparator.comparing(Task::getCompletedDate).reversed())
                .limit(LIST_LIMIT)
                .map(task -> TaskSummary.of(task, false))
                .toList();

        Map<com.homecloud.planner.shopping.PurchaseStatus, Long> purchaseStatusCounts = purchaseItems.stream()
                .collect(Collectors.groupingBy(PurchaseItem::getStatus, Collectors.counting()));
        Map<com.homecloud.planner.device.LifecycleStatus, Long> deviceLifecycleCounts = devices.stream()
                .collect(Collectors.groupingBy(Device::getLifecycleStatus, Collectors.counting()));
        Map<com.homecloud.planner.servicecatalog.ManagedServiceStatus, Long> serviceStatusCounts = services.stream()
                .collect(Collectors.groupingBy(ManagedService::getStatus, Collectors.counting()));

        BackupCoverageWarningCounts backupCoverageWarnings = new BackupCoverageWarningCounts(
                backupPolicies.stream().filter(backupCoverageCalculator::isMissingLocalBackup).count(),
                backupPolicies.stream().filter(backupCoverageCalculator::isMissingOffsiteBackup).count(),
                backupPolicies.stream().filter(backupCoverageCalculator::isMissingEncryptionForSensitiveOffsite).count(),
                backupPolicies.stream().filter(backupCoverageCalculator::isVerificationOverdue).count());

        List<WarrantyExpirationSummary> upcomingWarrantyExpirations = java.util.stream.Stream.concat(
                        devices.stream()
                                .filter(device -> isExpiringSoon(device.getWarrantyExpiration(), today))
                                .map(device -> new WarrantyExpirationSummary(
                                        WarrantyExpirationSummary.DEVICE, device.getId(), device.getName(), device.getWarrantyExpiration())),
                        purchaseItems.stream()
                                .filter(item -> isExpiringSoon(item.getWarrantyExpiration(), today))
                                .map(item -> new WarrantyExpirationSummary(
                                        WarrantyExpirationSummary.PURCHASE_ITEM,
                                        item.getId(),
                                        item.getProductName(),
                                        item.getWarrantyExpiration())))
                .sorted(Comparator.comparing(WarrantyExpirationSummary::warrantyExpiration))
                .limit(LIST_LIMIT)
                .toList();

        List<RecommendedAction> recommendedActions = recommendedActionEngine.recommend(
                tasks, blockedFlags, dependsOnByTask, phases, backupPolicies, backupCoverageCalculator, devices,
                purchaseItems, today);

        return new DashboardSummary(
                project.getId(),
                project.getName(),
                overallProgress,
                currentPhase,
                project.getBudget(),
                budgetSummary.estimatedTotal(),
                budgetSummary.actualTotal(),
                budgetSummary.remainingBudget(),
                blockedTaskSummaries.size(),
                blockedTaskSummaries.stream().limit(LIST_LIMIT).toList(),
                upcomingTargetDates,
                recentCompletedTasks,
                purchaseStatusCounts,
                deviceLifecycleCounts,
                serviceStatusCounts,
                backupCoverageWarnings,
                upcomingWarrantyExpirations,
                recommendedActions);
    }

    private PhaseSummary findCurrentPhase(List<Phase> phases, List<Task> tasks, Map<UUID, Boolean> blockedFlags) {
        Map<UUID, List<Task>> tasksByPhase = tasks.stream().collect(Collectors.groupingBy(task -> task.getPhase().getId()));
        return phases.stream()
                .sorted(Comparator.comparingInt(Phase::getSequence))
                .filter(phase -> tasksByPhase.getOrDefault(phase.getId(), List.of()).stream()
                        .anyMatch(task -> task.isEligibleForProgress()
                                && task.getStatus() != com.homecloud.planner.task.TaskStatus.COMPLETED))
                .findFirst()
                .map(phase -> {
                    List<Task> phaseTasks = tasksByPhase.getOrDefault(phase.getId(), List.of());
                    ProgressSummary progress = taskProgressCalculator.summarize(phaseTasks, blockedFlags);
                    return PhaseSummary.of(phase, progress);
                })
                .orElse(null);
    }

    private boolean isExpiringSoon(LocalDate expiration, LocalDate today) {
        if (expiration == null) {
            return false;
        }
        long daysUntilExpiration = java.time.temporal.ChronoUnit.DAYS.between(today, expiration);
        return daysUntilExpiration >= 0 && daysUntilExpiration <= WARRANTY_LOOKAHEAD_DAYS;
    }
}
