package com.homecloud.planner.export;

import com.homecloud.planner.backup.BackupPolicyFilter;
import com.homecloud.planner.backup.BackupPolicyService;
import com.homecloud.planner.decision.ArchitectureDecisionFilter;
import com.homecloud.planner.decision.ArchitectureDecisionService;
import com.homecloud.planner.device.DeviceFilter;
import com.homecloud.planner.device.DeviceService;
import com.homecloud.planner.phase.PhaseService;
import com.homecloud.planner.project.ProjectResponse;
import com.homecloud.planner.project.ProjectService;
import com.homecloud.planner.servicecatalog.ManagedServiceFilter;
import com.homecloud.planner.servicecatalog.ManagedServiceResponse;
import com.homecloud.planner.servicecatalog.ManagedServiceService;
import com.homecloud.planner.shopping.PurchaseItemFilter;
import com.homecloud.planner.shopping.PurchaseItemService;
import com.homecloud.planner.task.TaskFilter;
import com.homecloud.planner.task.TaskResponse;
import com.homecloud.planner.task.TaskService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Assembles a complete, consistent export of every project by reusing the
 * same list methods (and therefore the same response DTOs) the REST API
 * exposes elsewhere. Nothing here touches authentication data — there is
 * none yet, and this must stay true once Milestone 7 adds it (see
 * ADR-0007).
 */
@Service
public class ExportService {

    private final ProjectService projectService;
    private final PhaseService phaseService;
    private final TaskService taskService;
    private final PurchaseItemService purchaseItemService;
    private final DeviceService deviceService;
    private final ManagedServiceService managedServiceService;
    private final BackupPolicyService backupPolicyService;
    private final ArchitectureDecisionService architectureDecisionService;
    private final Clock clock;

    public ExportService(
            ProjectService projectService,
            PhaseService phaseService,
            TaskService taskService,
            PurchaseItemService purchaseItemService,
            DeviceService deviceService,
            ManagedServiceService managedServiceService,
            BackupPolicyService backupPolicyService,
            ArchitectureDecisionService architectureDecisionService,
            Clock clock) {
        this.projectService = projectService;
        this.phaseService = phaseService;
        this.taskService = taskService;
        this.purchaseItemService = purchaseItemService;
        this.deviceService = deviceService;
        this.managedServiceService = managedServiceService;
        this.backupPolicyService = backupPolicyService;
        this.architectureDecisionService = architectureDecisionService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ExportBundle buildExport() {
        List<ProjectExport> projects = projectService.listProjects().stream().map(this::exportProject).toList();
        return new ExportBundle(Instant.now(clock), projects);
    }

    private ProjectExport exportProject(ProjectResponse project) {
        List<TaskResponse> tasks = taskService.listTasks(project.id(), TaskFilter.NONE);
        List<ManagedServiceResponse> services = managedServiceService.listServices(project.id(), ManagedServiceFilter.NONE);

        List<DependencyEdge> taskDependencies = tasks.stream()
                .flatMap(task -> task.dependsOnTaskIds().stream().map(dependsOnId -> new DependencyEdge(task.id(), dependsOnId)))
                .toList();
        List<DependencyEdge> serviceDependencies = services.stream()
                .flatMap(service -> service.dependsOnServiceIds().stream()
                        .map(dependsOnId -> new DependencyEdge(service.id(), dependsOnId)))
                .toList();

        return new ProjectExport(
                project,
                phaseService.listPhases(project.id()),
                tasks,
                taskDependencies,
                purchaseItemService.listPurchaseItems(project.id(), PurchaseItemFilter.NONE),
                deviceService.listDevices(project.id(), DeviceFilter.NONE),
                services,
                serviceDependencies,
                backupPolicyService.listBackupPolicies(project.id(), BackupPolicyFilter.NONE),
                architectureDecisionService.listDecisions(project.id(), ArchitectureDecisionFilter.NONE));
    }
}
