package com.homecloud.planner.dashboard;

public record BackupCoverageWarningCounts(
        long missingLocalBackupCount,
        long missingOffsiteBackupCount,
        long missingEncryptionForSensitiveOffsiteCount,
        long verificationOverdueCount) {
}
