package com.homecloud.planner.decision;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ArchitectureDecisionRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull DecisionStatus status,
        String context,
        @NotBlank String decision,
        String alternativesConsidered,
        String consequences,
        LocalDate decisionDate,
        String revisitCriteria,
        List<UUID> relatedDeviceIds,
        List<UUID> relatedServiceIds) {
}
