package com.homecloud.planner.servicecatalog;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ManagedServiceResponse(
        UUID id,
        UUID projectId,
        UUID hostDeviceId,
        String name,
        String purpose,
        String description,
        ManagedServiceStatus status,
        RuntimeType runtimeType,
        String storageLocation,
        Sensitivity sensitivity,
        boolean externallyExposed,
        String authenticationMethod,
        String backupPolicy,
        String documentationUrl,
        String repositoryUrl,
        String notes,
        List<UUID> dependsOnServiceIds,
        Instant createdAt,
        Instant updatedAt) {

    public static ManagedServiceResponse of(ManagedService service, List<UUID> dependsOnServiceIds) {
        return new ManagedServiceResponse(
                service.getId(),
                service.getProject().getId(),
                service.getHostDevice() == null ? null : service.getHostDevice().getId(),
                service.getName(),
                service.getPurpose(),
                service.getDescription(),
                service.getStatus(),
                service.getRuntimeType(),
                service.getStorageLocation(),
                service.getSensitivity(),
                service.isExternallyExposed(),
                service.getAuthenticationMethod(),
                service.getBackupPolicy(),
                service.getDocumentationUrl(),
                service.getRepositoryUrl(),
                service.getNotes(),
                dependsOnServiceIds,
                service.getCreatedAt(),
                service.getUpdatedAt());
    }
}
