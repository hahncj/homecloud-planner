package com.homecloud.planner.servicecatalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.hibernate.validator.constraints.URL;

public record ManagedServiceRequest(
        UUID hostDeviceId,
        @NotBlank @Size(max = 200) String name,
        String purpose,
        String description,
        @NotNull ManagedServiceStatus status,
        @NotNull RuntimeType runtimeType,
        String storageLocation,
        @NotNull Sensitivity sensitivity,
        boolean externallyExposed,
        String authenticationMethod,
        String backupPolicy,
        @URL(message = "documentationUrl must be a valid URL") String documentationUrl,
        @URL(message = "repositoryUrl must be a valid URL") String repositoryUrl,
        String notes) {
}
