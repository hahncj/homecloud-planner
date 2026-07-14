package com.homecloud.planner.roadmap;

public record ProgressSummary(long taskCount, long completedCount, long blockedCount, int progressPercentage) {

    public static final ProgressSummary EMPTY = new ProgressSummary(0, 0, 0, 0);
}
