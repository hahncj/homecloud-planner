package com.homecloud.planner.servicecatalog;

import java.time.Instant;
import java.util.UUID;

public record ServiceDependencyResponse(UUID serviceId, UUID dependsOnServiceId, Instant createdAt) {

    public static ServiceDependencyResponse of(ServiceDependency dependency) {
        return new ServiceDependencyResponse(
                dependency.getService().getId(), dependency.getDependsOnService().getId(), dependency.getCreatedAt());
    }
}
