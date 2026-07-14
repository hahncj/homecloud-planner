package com.homecloud.planner.backup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BackupPolicyRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 200) String dataCategory,
        @NotBlank @Size(max = 200) String primaryLocation,
        String localBackupLocation,
        String offsiteBackupLocation,
        boolean encrypted,
        boolean containsSensitiveData,
        @NotNull BackupFrequency frequency,
        String retention,
        String recoveryPointObjective,
        String recoveryTimeObjective,
        LocalDate lastVerifiedDate,
        String verificationNotes) {
}
