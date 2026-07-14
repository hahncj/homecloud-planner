package com.homecloud.planner.project;

import com.homecloud.planner.roadmap.ProgressSummary;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        ProjectStatus status,
        BigDecimal budget,
        LocalDate startDate,
        LocalDate targetDate,
        Instant createdAt,
        Instant updatedAt,
        ProgressSummary progress) {

    public static ProjectResponse of(Project project, ProgressSummary progress) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getBudget(),
                project.getStartDate(),
                project.getTargetDate(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                progress);
    }
}
