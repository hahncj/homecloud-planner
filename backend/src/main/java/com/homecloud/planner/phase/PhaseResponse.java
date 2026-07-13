package com.homecloud.planner.phase;

import com.homecloud.planner.roadmap.ProgressSummary;
import java.time.Instant;
import java.util.UUID;

public record PhaseResponse(
        UUID id,
        UUID projectId,
        String name,
        String description,
        int sequence,
        Instant createdAt,
        Instant updatedAt,
        ProgressSummary progress) {

    public static PhaseResponse of(Phase phase, ProgressSummary progress) {
        return new PhaseResponse(
                phase.getId(),
                phase.getProject().getId(),
                phase.getName(),
                phase.getDescription(),
                phase.getSequence(),
                phase.getCreatedAt(),
                phase.getUpdatedAt(),
                progress);
    }
}
