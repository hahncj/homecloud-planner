package com.homecloud.planner.backup;

import static org.assertj.core.api.Assertions.assertThat;

import com.homecloud.planner.project.Project;
import com.homecloud.planner.project.ProjectStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class BackupCoverageCalculatorTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-07-13T00:00:00Z");
    private final Clock clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
    private final BackupCoverageCalculator calculator = new BackupCoverageCalculator(clock);

    private static Project project() {
        return new Project("Personal Hybrid Cloud", null, ProjectStatus.IN_PROGRESS, null, null, null);
    }

    private static BackupPolicy policy(
            String localLocation, String offsiteLocation, boolean encrypted, boolean sensitive, LocalDate lastVerified) {
        BackupPolicy policy = new BackupPolicy(
                project(), "Photos", "Photos", "NAS volume1", localLocation, offsiteLocation, encrypted, sensitive,
                BackupFrequency.DAILY, null, null, null, lastVerified, null);
        ReflectionTestUtils.setField(policy, "id", java.util.UUID.randomUUID());
        return policy;
    }

    @Test
    void flagsMissingLocalBackup() {
        BackupPolicy policy = policy(null, "Backblaze B2", true, false, LocalDate.now(clock));
        assertThat(calculator.isMissingLocalBackup(policy)).isTrue();
        assertThat(calculator.isMissingOffsiteBackup(policy)).isFalse();
        assertThat(calculator.coverageState(policy)).isEqualTo(BackupCoverageState.PARTIAL);
    }

    @Test
    void flagsMissingOffsiteBackup() {
        BackupPolicy policy = policy("NAS volume2", null, true, false, LocalDate.now(clock));
        assertThat(calculator.isMissingOffsiteBackup(policy)).isTrue();
        assertThat(calculator.coverageState(policy)).isEqualTo(BackupCoverageState.PARTIAL);
    }

    @Test
    void noBackupLocationsIsNoneCoverage() {
        BackupPolicy policy = policy(null, null, false, false, null);
        assertThat(calculator.coverageState(policy)).isEqualTo(BackupCoverageState.NONE);
    }

    @Test
    void bothBackupLocationsIsFullCoverage() {
        BackupPolicy policy = policy("NAS volume2", "Backblaze B2", true, false, LocalDate.now(clock));
        assertThat(calculator.coverageState(policy)).isEqualTo(BackupCoverageState.FULL);
    }

    @Test
    void flagsMissingEncryptionOnlyForSensitiveDataWithOffsiteBackup() {
        BackupPolicy sensitiveUnencrypted = policy("NAS volume2", "Backblaze B2", false, true, LocalDate.now(clock));
        assertThat(calculator.isMissingEncryptionForSensitiveOffsite(sensitiveUnencrypted)).isTrue();

        BackupPolicy nonSensitiveUnencrypted = policy("NAS volume2", "Backblaze B2", false, false, LocalDate.now(clock));
        assertThat(calculator.isMissingEncryptionForSensitiveOffsite(nonSensitiveUnencrypted)).isFalse();

        BackupPolicy sensitiveEncrypted = policy("NAS volume2", "Backblaze B2", true, true, LocalDate.now(clock));
        assertThat(calculator.isMissingEncryptionForSensitiveOffsite(sensitiveEncrypted)).isFalse();

        BackupPolicy sensitiveNoOffsite = policy("NAS volume2", null, false, true, LocalDate.now(clock));
        assertThat(calculator.isMissingEncryptionForSensitiveOffsite(sensitiveNoOffsite)).isFalse();
    }

    @Test
    void neverVerifiedIsOverdue() {
        BackupPolicy policy = policy("NAS volume2", "Backblaze B2", true, false, null);
        assertThat(calculator.isVerificationOverdue(policy)).isTrue();
    }

    @Test
    void recentlyVerifiedIsNotOverdue() {
        BackupPolicy policy = policy("NAS volume2", "Backblaze B2", true, false, LocalDate.now(clock).minusDays(10));
        assertThat(calculator.isVerificationOverdue(policy)).isFalse();
    }

    @Test
    void verifiedBeyondThresholdIsOverdue() {
        BackupPolicy policy = policy(
                "NAS volume2", "Backblaze B2", true, false,
                LocalDate.now(clock).minusDays(BackupCoverageCalculator.VERIFICATION_OVERDUE_AFTER_DAYS + 1));
        assertThat(calculator.isVerificationOverdue(policy)).isTrue();
    }
}
