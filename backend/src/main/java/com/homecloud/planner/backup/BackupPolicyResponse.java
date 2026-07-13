package com.homecloud.planner.backup;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BackupPolicyResponse(
        UUID id,
        UUID projectId,
        String name,
        String dataCategory,
        String primaryLocation,
        String localBackupLocation,
        String offsiteBackupLocation,
        boolean encrypted,
        boolean containsSensitiveData,
        BackupFrequency frequency,
        String retention,
        String recoveryPointObjective,
        String recoveryTimeObjective,
        LocalDate lastVerifiedDate,
        String verificationNotes,
        BackupCoverageState coverageState,
        boolean missingLocalBackup,
        boolean missingOffsiteBackup,
        boolean missingEncryptionForSensitiveOffsite,
        boolean verificationOverdue,
        Instant createdAt,
        Instant updatedAt) {

    public static BackupPolicyResponse of(BackupPolicy policy, BackupCoverageCalculator calculator) {
        return new BackupPolicyResponse(
                policy.getId(),
                policy.getProject().getId(),
                policy.getName(),
                policy.getDataCategory(),
                policy.getPrimaryLocation(),
                policy.getLocalBackupLocation(),
                policy.getOffsiteBackupLocation(),
                policy.isEncrypted(),
                policy.isContainsSensitiveData(),
                policy.getFrequency(),
                policy.getRetention(),
                policy.getRecoveryPointObjective(),
                policy.getRecoveryTimeObjective(),
                policy.getLastVerifiedDate(),
                policy.getVerificationNotes(),
                calculator.coverageState(policy),
                calculator.isMissingLocalBackup(policy),
                calculator.isMissingOffsiteBackup(policy),
                calculator.isMissingEncryptionForSensitiveOffsite(policy),
                calculator.isVerificationOverdue(policy),
                policy.getCreatedAt(),
                policy.getUpdatedAt());
    }
}
