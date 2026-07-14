package com.homecloud.planner.backup;

/** Computed from whether local/off-site backup locations are present; never persisted. */
public enum BackupCoverageState {
    NONE,
    PARTIAL,
    FULL
}
