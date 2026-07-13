package com.homecloud.planner.decision;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ArchitectureDecisionResponse(
        UUID id,
        UUID projectId,
        String title,
        DecisionStatus status,
        String context,
        String decision,
        String alternativesConsidered,
        String consequences,
        LocalDate decisionDate,
        String revisitCriteria,
        List<RelatedEntitySummary> relatedDevices,
        List<RelatedEntitySummary> relatedServices,
        Instant createdAt,
        Instant updatedAt) {

    public static ArchitectureDecisionResponse of(ArchitectureDecision entity) {
        return new ArchitectureDecisionResponse(
                entity.getId(),
                entity.getProject().getId(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getContext(),
                entity.getDecision(),
                entity.getAlternativesConsidered(),
                entity.getConsequences(),
                entity.getDecisionDate(),
                entity.getRevisitCriteria(),
                entity.getRelatedDevices().stream()
                        .map(device -> new RelatedEntitySummary(device.getId(), device.getName()))
                        .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                        .toList(),
                entity.getRelatedServices().stream()
                        .map(service -> new RelatedEntitySummary(service.getId(), service.getName()))
                        .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                        .toList(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
