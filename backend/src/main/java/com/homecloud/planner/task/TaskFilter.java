package com.homecloud.planner.task;

import java.util.UUID;

public record TaskFilter(UUID phaseId, TaskStatus status, TaskPriority priority, Boolean blocked) {

    public static final TaskFilter NONE = new TaskFilter(null, null, null, null);
}
