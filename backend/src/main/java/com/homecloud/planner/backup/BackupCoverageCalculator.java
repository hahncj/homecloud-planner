package com.homecloud.planner.backup;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

/**
 * Computes backup-matrix warnings and coverage/verification state on the
 * fly rather than persisting them, so they always reflect the policy's
 * current fields. See ADR-0005 for the rules and the 90-day verification
 * threshold.
 */
@Component
public class BackupCoverageCalculator {

    static final int VERIFICATION_OVERDUE_AFTER_DAYS = 90;

    private final Clock clock;

    public BackupCoverageCalculator(Clock clock) {
        this.clock = clock;
    }

    public boolean isMissingLocalBackup(BackupPolicy policy) {
        return isBlank(policy.getLocalBackupLocation());
    }

    public boolean isMissingOffsiteBackup(BackupPolicy policy) {
        return isBlank(policy.getOffsiteBackupLocation());
    }

    /** Only flagged when the policy actually has an off-site location; an absent one is covered by the off-site warning instead. */
    public boolean isMissingEncryptionForSensitiveOffsite(BackupPolicy policy) {
        return policy.isContainsSensitiveData() && !isBlank(policy.getOffsiteBackupLocation()) && !policy.isEncrypted();
    }

    public boolean isVerificationOverdue(BackupPolicy policy) {
        LocalDate lastVerified = policy.getLastVerifiedDate();
        if (lastVerified == null) {
            return true;
        }
        LocalDate today = LocalDate.now(clock);
        return ChronoUnit.DAYS.between(lastVerified, today) > VERIFICATION_OVERDUE_AFTER_DAYS;
    }

    public BackupCoverageState coverageState(BackupPolicy policy) {
        boolean hasLocal = !isMissingLocalBackup(policy);
        boolean hasOffsite = !isMissingOffsiteBackup(policy);
        if (hasLocal && hasOffsite) {
            return BackupCoverageState.FULL;
        }
        if (hasLocal || hasOffsite) {
            return BackupCoverageState.PARTIAL;
        }
        return BackupCoverageState.NONE;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
