package com.homecloud.planner.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.homecloud.planner.backup.BackupCoverageCalculator;
import com.homecloud.planner.backup.BackupFrequency;
import com.homecloud.planner.backup.BackupPolicy;
import com.homecloud.planner.device.Device;
import com.homecloud.planner.device.LifecycleStatus;
import com.homecloud.planner.phase.Phase;
import com.homecloud.planner.project.Project;
import com.homecloud.planner.project.ProjectStatus;
import com.homecloud.planner.shopping.PurchaseItem;
import com.homecloud.planner.shopping.PurchaseStatus;
import com.homecloud.planner.task.Task;
import com.homecloud.planner.task.TaskPriority;
import com.homecloud.planner.task.TaskStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RecommendedActionEngineTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-07-13T00:00:00Z");
    private final Clock clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
    private final LocalDate today = LocalDate.now(clock);
    private final RecommendedActionEngine engine = new RecommendedActionEngine();
    private final BackupCoverageCalculator backupCalculator = new BackupCoverageCalculator(clock);

    private static Project project() {
        Project project = new Project("Personal Hybrid Cloud", null, ProjectStatus.IN_PROGRESS, null, null, null);
        ReflectionTestUtils.setField(project, "id", UUID.randomUUID());
        return project;
    }

    private static Phase phase(Project project, String name, int sequence) {
        Phase phase = new Phase(project, name, null, sequence);
        ReflectionTestUtils.setField(phase, "id", UUID.randomUUID());
        return phase;
    }

    private static Task task(Phase phase, String title, TaskStatus status) {
        Task task = new Task(phase, title, null, status, TaskPriority.MEDIUM, null, null, null, null, null, null);
        ReflectionTestUtils.setField(task, "id", UUID.randomUUID());
        return task;
    }

    private static Device device(Project project, String name, LocalDate warrantyExpiration) {
        Device device = new Device(
                project, name, null, null, null, null, null, null, null, null, null, null, null, null,
                warrantyExpiration, LifecycleStatus.ACTIVE, null, null);
        ReflectionTestUtils.setField(device, "id", UUID.randomUUID());
        return device;
    }

    private static PurchaseItem purchaseItem(Project project, Phase phase, String productName, PurchaseStatus status) {
        PurchaseItem item = new PurchaseItem(
                project, phase, "Category", productName, null, null, null, 1, null, null, null, null, status, null,
                null, null, null, null);
        ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
        return item;
    }

    private static BackupPolicy backupPolicy(Project project, String category, LocalDate lastVerified) {
        BackupPolicy policy = new BackupPolicy(
                project, category + " backup", category, "Primary", "Local", "Offsite", true, false,
                BackupFrequency.DAILY, null, null, null, lastVerified, null);
        ReflectionTestUtils.setField(policy, "id", UUID.randomUUID());
        return policy;
    }

    @Test
    void recommendsCompletingBlockingTaskBeforeDependentTask() {
        Project project = project();
        Phase phase = phase(project, "Foundation", 1);
        Task prerequisite = task(phase, "Rack the switch", TaskStatus.NOT_STARTED);
        Task blocked = task(phase, "Configure VLANs", TaskStatus.NOT_STARTED);

        List<RecommendedAction> actions = engine.recommend(
                List.of(prerequisite, blocked),
                Map.of(prerequisite.getId(), false, blocked.getId(), true),
                Map.of(blocked.getId(), List.of(prerequisite.getId())),
                List.of(phase),
                List.of(),
                backupCalculator,
                List.of(),
                List.of(),
                today);

        assertThat(actions).anySatisfy(action -> {
            assertThat(action.category()).isEqualTo("BLOCKING_TASK");
            assertThat(action.message()).contains("Rack the switch").contains("Configure VLANs");
        });
    }

    @Test
    void recommendsVerifyingOverdueBackups() {
        Project project = project();
        BackupPolicy neverVerified = backupPolicy(project, "Photos", null);
        BackupPolicy recentlyVerified = backupPolicy(project, "Documents", today.minusDays(5));

        List<RecommendedAction> actions = engine.recommend(
                List.of(), Map.of(), Map.of(), List.of(), List.of(neverVerified, recentlyVerified), backupCalculator,
                List.of(), List.of(), today);

        assertThat(actions)
                .anySatisfy(action -> {
                    assertThat(action.category()).isEqualTo("BACKUP_VERIFICATION");
                    assertThat(action.message()).contains("Photos");
                })
                .noneSatisfy(action -> assertThat(action.message()).contains("Documents"));
    }

    @Test
    void recommendsReviewingWarrantiesExpiringWithinThirtyDays() {
        Project project = project();
        Device expiringSoon = device(project, "NAS", today.plusDays(10));
        Device expiringLater = device(project, "Switch", today.plusDays(200));
        Phase phase = phase(project, "Storage", 1);
        PurchaseItem expiringItem = purchaseItem(project, phase, "UPS", PurchaseStatus.RECEIVED);
        ReflectionTestUtils.setField(expiringItem, "warrantyExpiration", today.plusDays(5));

        List<RecommendedAction> actions = engine.recommend(
                List.of(), Map.of(), Map.of(), List.of(), List.of(), backupCalculator,
                List.of(expiringSoon, expiringLater), List.of(expiringItem), today);

        assertThat(actions)
                .anySatisfy(action -> {
                    assertThat(action.category()).isEqualTo("WARRANTY");
                    assertThat(action.message()).contains("NAS");
                })
                .anySatisfy(action -> assertThat(action.message()).contains("UPS"))
                .noneSatisfy(action -> assertThat(action.message()).contains("Switch"));
    }

    @Test
    void recommendsFinishingCurrentPhaseWhenLaterPhaseHasStartedWork() {
        Project project = project();
        Phase foundation = phase(project, "Foundation", 1);
        Phase storage = phase(project, "Storage", 2);
        Task incompleteFoundationTask = task(foundation, "Document VLAN plan", TaskStatus.NOT_STARTED);
        Task startedStorageTask = task(storage, "Select NAS", TaskStatus.IN_PROGRESS);

        List<RecommendedAction> actions = engine.recommend(
                List.of(incompleteFoundationTask, startedStorageTask),
                Map.of(incompleteFoundationTask.getId(), false, startedStorageTask.getId(), false),
                Map.of(),
                List.of(foundation, storage),
                List.of(),
                backupCalculator,
                List.of(),
                List.of(),
                today);

        assertThat(actions).anySatisfy(action -> {
            assertThat(action.category()).isEqualTo("PHASE_FOCUS");
            assertThat(action.message()).contains("Foundation");
        });
    }

    @Test
    void doesNotRecommendPhaseFocusWhenNoLaterPhaseHasStartedWork() {
        Project project = project();
        Phase foundation = phase(project, "Foundation", 1);
        Phase storage = phase(project, "Storage", 2);
        Task incompleteFoundationTask = task(foundation, "Document VLAN plan", TaskStatus.NOT_STARTED);
        Task notStartedStorageTask = task(storage, "Select NAS", TaskStatus.NOT_STARTED);

        List<RecommendedAction> actions = engine.recommend(
                List.of(incompleteFoundationTask, notStartedStorageTask),
                Map.of(incompleteFoundationTask.getId(), false, notStartedStorageTask.getId(), false),
                Map.of(),
                List.of(foundation, storage),
                List.of(),
                backupCalculator,
                List.of(),
                List.of(),
                today);

        assertThat(actions).noneMatch(action -> action.category().equals("PHASE_FOCUS"));
    }

    @Test
    void recommendsResolvingPendingPurchasesBlockingReadyTasks() {
        Project project = project();
        Phase storage = phase(project, "Storage", 1);
        Task readyTask = task(storage, "Configure Time Machine", TaskStatus.NOT_STARTED);
        PurchaseItem pendingPurchase = purchaseItem(project, storage, "NAS", PurchaseStatus.PLANNED);

        List<RecommendedAction> actions = engine.recommend(
                List.of(readyTask),
                Map.of(readyTask.getId(), false),
                Map.of(),
                List.of(storage),
                List.of(),
                backupCalculator,
                List.of(),
                List.of(pendingPurchase),
                today);

        assertThat(actions).anySatisfy(action -> {
            assertThat(action.category()).isEqualTo("PENDING_PURCHASE");
            assertThat(action.message()).contains("Storage");
        });
    }

    @Test
    void doesNotRecommendPendingPurchaseWhenTheOnlyReadyTaskIsBlocked() {
        Project project = project();
        Phase storage = phase(project, "Storage", 1);
        Task blockedTask = task(storage, "Configure Time Machine", TaskStatus.NOT_STARTED);
        PurchaseItem pendingPurchase = purchaseItem(project, storage, "NAS", PurchaseStatus.PLANNED);

        List<RecommendedAction> actions = engine.recommend(
                List.of(blockedTask),
                Map.of(blockedTask.getId(), true),
                Map.of(),
                List.of(storage),
                List.of(),
                backupCalculator,
                List.of(),
                List.of(pendingPurchase),
                today);

        assertThat(actions).noneMatch(action -> action.category().equals("PENDING_PURCHASE"));
    }
}
