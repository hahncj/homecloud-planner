package com.homecloud.planner.task;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID phaseId,
        UUID projectId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        BigDecimal estimatedCost,
        BigDecimal actualCost,
        LocalDate targetDate,
        LocalDate completedDate,
        String acceptanceCriteria,
        String notes,
        boolean blocked,
        List<UUID> dependsOnTaskIds,
        Instant createdAt,
        Instant updatedAt) {

    public static TaskResponse of(Task task, boolean blocked, List<UUID> dependsOnTaskIds) {
        return new TaskResponse(
                task.getId(),
                task.getPhase().getId(),
                task.getPhase().getProject().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getEstimatedCost(),
                task.getActualCost(),
                task.getTargetDate(),
                task.getCompletedDate(),
                task.getAcceptanceCriteria(),
                task.getNotes(),
                blocked,
                dependsOnTaskIds,
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
