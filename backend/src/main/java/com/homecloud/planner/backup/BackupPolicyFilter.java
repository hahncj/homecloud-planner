package com.homecloud.planner.backup;

public record BackupPolicyFilter(BackupCoverageState coverageState, Boolean verificationOverdue) {

    public static final BackupPolicyFilter NONE = new BackupPolicyFilter(null, null);
}
