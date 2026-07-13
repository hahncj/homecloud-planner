package com.homecloud.planner.task;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TaskDependencyRequest(@NotNull UUID dependsOnTaskId) {
}
