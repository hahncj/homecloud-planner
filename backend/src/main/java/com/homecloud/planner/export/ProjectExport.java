package com.homecloud.planner.export;

import com.homecloud.planner.backup.BackupPolicyResponse;
import com.homecloud.planner.decision.ArchitectureDecisionResponse;
import com.homecloud.planner.device.DeviceResponse;
import com.homecloud.planner.phase.PhaseResponse;
import com.homecloud.planner.project.ProjectResponse;
import com.homecloud.planner.servicecatalog.ManagedServiceResponse;
import com.homecloud.planner.shopping.PurchaseItemResponse;
import com.homecloud.planner.task.TaskResponse;
import java.util.List;

/**
 * Everything belonging to one project, reusing the same response DTOs the
 * REST API already returns (never the JPA entities directly) so the export
 * shape stays consistent with the API and never risks leaking a field that
 * hasn't already been reviewed for the API contract.
 */
public record ProjectExport(
        ProjectResponse project,
        List<PhaseResponse> phases,
        List<TaskResponse> tasks,
        List<DependencyEdge> taskDependencies,
        List<PurchaseItemResponse> purchaseItems,
        List<DeviceResponse> devices,
        List<ManagedServiceResponse> services,
        List<DependencyEdge> serviceDependencies,
        List<BackupPolicyResponse> backupPolicies,
        List<ArchitectureDecisionResponse> decisions) {
}
