package com.homecloud.planner.servicecatalog;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ServiceDependencyRequest(@NotNull UUID dependsOnServiceId) {
}
