package com.homecloud.planner.export;

import com.homecloud.planner.backup.BackupPolicyResponse;
import com.homecloud.planner.decision.ArchitectureDecisionResponse;
import com.homecloud.planner.decision.RelatedEntitySummary;
import com.homecloud.planner.device.DeviceResponse;
import com.homecloud.planner.phase.PhaseResponse;
import com.homecloud.planner.servicecatalog.ManagedServiceResponse;
import com.homecloud.planner.shopping.PurchaseItemResponse;
import com.homecloud.planner.task.TaskResponse;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Renders an {@link ExportBundle} as a single, readable Markdown document. */
@Component
public class MarkdownExportRenderer {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_INSTANT;

    public String render(ExportBundle bundle) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# HomeCloud Planner Export\n\n");
        markdown.append("Exported: ").append(TIMESTAMP_FORMAT.format(bundle.exportedAt())).append("\n\n");

        if (bundle.projects().isEmpty()) {
            markdown.append("_No projects yet._\n");
            return markdown.toString();
        }

        for (ProjectExport export : bundle.projects()) {
            renderProject(markdown, export);
        }
        return markdown.toString();
    }

    private void renderProject(StringBuilder markdown, ProjectExport export) {
        var project = export.project();
        markdown.append("## ").append(project.name()).append("\n\n");
        markdown.append("- Status: ").append(project.status()).append('\n');
        markdown.append("- Budget: ").append(project.budget() == null ? "_not set_" : project.budget()).append('\n');
        markdown.append("- Start date: ").append(orDash(project.startDate())).append('\n');
        markdown.append("- Target date: ").append(orDash(project.targetDate())).append('\n');
        markdown.append("- Progress: ")
                .append(project.progress().progressPercentage())
                .append("% (")
                .append(project.progress().completedCount())
                .append('/')
                .append(project.progress().taskCount())
                .append(" tasks complete, ")
                .append(project.progress().blockedCount())
                .append(" blocked)\n\n");
        if (project.description() != null) {
            markdown.append(project.description()).append("\n\n");
        }

        renderPhases(markdown, export.phases());
        renderTasks(markdown, export.phases(), export.tasks());
        renderTaskDependencies(markdown, export.tasks(), export.taskDependencies());
        renderPurchaseItems(markdown, export.purchaseItems());
        renderDevices(markdown, export.devices());
        renderServices(markdown, export.services());
        renderServiceDependencies(markdown, export.services(), export.serviceDependencies());
        renderBackupPolicies(markdown, export.backupPolicies());
        renderDecisions(markdown, export.decisions());
    }

    private void renderPhases(StringBuilder markdown, List<PhaseResponse> phases) {
        markdown.append("### Phases\n\n");
        if (phases.isEmpty()) {
            markdown.append("_No phases yet._\n\n");
            return;
        }
        markdown.append("| # | Name | Progress | Tasks | Completed | Blocked |\n");
        markdown.append("|---|---|---|---|---|---|\n");
        for (PhaseResponse phase : phases) {
            markdown.append("| ").append(phase.sequence())
                    .append(" | ").append(phase.name())
                    .append(" | ").append(phase.progress().progressPercentage()).append('%')
                    .append(" | ").append(phase.progress().taskCount())
                    .append(" | ").append(phase.progress().completedCount())
                    .append(" | ").append(phase.progress().blockedCount())
                    .append(" |\n");
        }
        markdown.append('\n');
    }

    private void renderTasks(StringBuilder markdown, List<PhaseResponse> phases, List<TaskResponse> tasks) {
        markdown.append("### Tasks\n\n");
        if (tasks.isEmpty()) {
            markdown.append("_No tasks yet._\n\n");
            return;
        }
        Map<UUID, List<TaskResponse>> tasksByPhase = tasks.stream().collect(Collectors.groupingBy(TaskResponse::phaseId));
        for (PhaseResponse phase : phases) {
            List<TaskResponse> phaseTasks = tasksByPhase.get(phase.id());
            if (phaseTasks == null || phaseTasks.isEmpty()) {
                continue;
            }
            markdown.append("#### ").append(phase.name()).append("\n\n");
            for (TaskResponse task : phaseTasks) {
                String checkbox = task.status().name().equals("COMPLETED") ? "[x]" : "[ ]";
                markdown.append("- ").append(checkbox).append(' ').append(task.title())
                        .append(" (").append(task.status()).append(", ").append(task.priority()).append(')');
                if (task.blocked()) {
                    markdown.append(" — BLOCKED");
                }
                markdown.append('\n');
            }
            markdown.append('\n');
        }
    }

    private void renderTaskDependencies(StringBuilder markdown, List<TaskResponse> tasks, List<DependencyEdge> edges) {
        markdown.append("### Task Dependencies\n\n");
        if (edges.isEmpty()) {
            markdown.append("_No dependencies yet._\n\n");
            return;
        }
        Map<UUID, String> titleById = tasks.stream().collect(Collectors.toMap(TaskResponse::id, TaskResponse::title));
        for (DependencyEdge edge : edges) {
            markdown.append("- ").append(titleById.getOrDefault(edge.from(), edge.from().toString()))
                    .append(" depends on ")
                    .append(titleById.getOrDefault(edge.to(), edge.to().toString()))
                    .append('\n');
        }
        markdown.append('\n');
    }

    private void renderPurchaseItems(StringBuilder markdown, List<PurchaseItemResponse> items) {
        markdown.append("### Shopping List\n\n");
        if (items.isEmpty()) {
            markdown.append("_No purchase items yet._\n\n");
            return;
        }
        markdown.append("| Category | Product | Status | Qty | Estimated | Actual |\n");
        markdown.append("|---|---|---|---|---|---|\n");
        for (PurchaseItemResponse item : items) {
            markdown.append("| ").append(item.category())
                    .append(" | ").append(item.productName())
                    .append(" | ").append(item.status())
                    .append(" | ").append(item.quantity())
                    .append(" | ").append(orDash(item.estimatedTotal()))
                    .append(" | ").append(orDash(item.actualTotal()))
                    .append(" |\n");
        }
        markdown.append('\n');
    }

    private void renderDevices(StringBuilder markdown, List<DeviceResponse> devices) {
        markdown.append("### Hardware Inventory\n\n");
        if (devices.isEmpty()) {
            markdown.append("_No devices yet._\n\n");
            return;
        }
        markdown.append("| Name | Role | Location | Lifecycle |\n");
        markdown.append("|---|---|---|---|\n");
        for (DeviceResponse device : devices) {
            markdown.append("| ").append(device.name())
                    .append(" | ").append(orDash(device.role()))
                    .append(" | ").append(orDash(device.location()))
                    .append(" | ").append(device.lifecycleStatus())
                    .append(" |\n");
        }
        markdown.append('\n');
    }

    private void renderServices(StringBuilder markdown, List<ManagedServiceResponse> services) {
        markdown.append("### Service Catalog\n\n");
        if (services.isEmpty()) {
            markdown.append("_No services yet._\n\n");
            return;
        }
        markdown.append("| Name | Status | Runtime | Sensitivity | Externally exposed |\n");
        markdown.append("|---|---|---|---|---|\n");
        for (ManagedServiceResponse service : services) {
            markdown.append("| ").append(service.name())
                    .append(" | ").append(service.status())
                    .append(" | ").append(service.runtimeType())
                    .append(" | ").append(service.sensitivity())
                    .append(" | ").append(service.externallyExposed() ? "Yes" : "No")
                    .append(" |\n");
        }
        markdown.append('\n');
    }

    private void renderServiceDependencies(StringBuilder markdown, List<ManagedServiceResponse> services, List<DependencyEdge> edges) {
        markdown.append("### Service Dependencies\n\n");
        if (edges.isEmpty()) {
            markdown.append("_No dependencies yet._\n\n");
            return;
        }
        Map<UUID, String> nameById = services.stream().collect(Collectors.toMap(ManagedServiceResponse::id, ManagedServiceResponse::name));
        for (DependencyEdge edge : edges) {
            markdown.append("- ").append(nameById.getOrDefault(edge.from(), edge.from().toString()))
                    .append(" depends on ")
                    .append(nameById.getOrDefault(edge.to(), edge.to().toString()))
                    .append('\n');
        }
        markdown.append('\n');
    }

    private void renderBackupPolicies(StringBuilder markdown, List<BackupPolicyResponse> policies) {
        markdown.append("### Backup Matrix\n\n");
        markdown.append("_RAID and snapshots are not backups on their own — see the coverage column below._\n\n");
        if (policies.isEmpty()) {
            markdown.append("_No backup policies yet._\n\n");
            return;
        }
        markdown.append("| Category | Coverage | Local | Off-site | Encrypted | Verification |\n");
        markdown.append("|---|---|---|---|---|---|\n");
        for (BackupPolicyResponse policy : policies) {
            markdown.append("| ").append(policy.dataCategory())
                    .append(" | ").append(policy.coverageState())
                    .append(" | ").append(policy.missingLocalBackup() ? "Missing" : "OK")
                    .append(" | ").append(policy.missingOffsiteBackup() ? "Missing" : "OK")
                    .append(" | ").append(policy.encrypted() ? "Yes" : "No")
                    .append(" | ").append(policy.verificationOverdue() ? "Overdue" : "Verified")
                    .append(" |\n");
        }
        markdown.append('\n');
    }

    private void renderDecisions(StringBuilder markdown, List<ArchitectureDecisionResponse> decisions) {
        markdown.append("### Architecture Decisions\n\n");
        if (decisions.isEmpty()) {
            markdown.append("_No decisions yet._\n\n");
            return;
        }
        List<ArchitectureDecisionResponse> sorted = decisions.stream()
                .sorted(Comparator.comparing(d -> d.decisionDate() == null ? java.time.LocalDate.MIN : d.decisionDate()))
                .toList();
        for (ArchitectureDecisionResponse decision : sorted) {
            markdown.append("#### ").append(decision.title()).append(" (").append(decision.status()).append(")\n\n");
            if (decision.decisionDate() != null) {
                markdown.append("Decided: ").append(decision.decisionDate()).append("\n\n");
            }
            appendSection(markdown, "Context", decision.context());
            appendSection(markdown, "Decision", decision.decision());
            appendSection(markdown, "Alternatives considered", decision.alternativesConsidered());
            appendSection(markdown, "Consequences", decision.consequences());
            appendSection(markdown, "Revisit criteria", decision.revisitCriteria());
            appendRelated(markdown, "Related devices", decision.relatedDevices());
            appendRelated(markdown, "Related services", decision.relatedServices());
        }
    }

    private void appendSection(StringBuilder markdown, String heading, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        markdown.append("**").append(heading).append(":** ").append(content).append("\n\n");
    }

    private void appendRelated(StringBuilder markdown, String heading, List<RelatedEntitySummary> entities) {
        if (entities.isEmpty()) {
            return;
        }
        markdown.append("**").append(heading).append(":** ")
                .append(entities.stream().map(RelatedEntitySummary::name).collect(Collectors.joining(", ")))
                .append("\n\n");
    }

    private String orDash(Object value) {
        return value == null ? "—" : value.toString();
    }
}
