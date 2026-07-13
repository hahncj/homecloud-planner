package com.homecloud.planner.task;

import java.time.Instant;
import java.util.UUID;

public record TaskDependencyResponse(UUID taskId, UUID dependsOnTaskId, Instant createdAt) {

    public static TaskDependencyResponse of(TaskDependency dependency) {
        return new TaskDependencyResponse(
                dependency.getTask().getId(), dependency.getDependsOnTask().getId(), dependency.getCreatedAt());
    }
}
