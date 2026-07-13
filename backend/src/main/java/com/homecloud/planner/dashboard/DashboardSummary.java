package com.homecloud.planner.dashboard;

import com.homecloud.planner.device.LifecycleStatus;
import com.homecloud.planner.roadmap.ProgressSummary;
import com.homecloud.planner.servicecatalog.ManagedServiceStatus;
import com.homecloud.planner.shopping.PurchaseStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DashboardSummary(
        UUID projectId,
        String projectName,
        ProgressSummary overallProgress,
        PhaseSummary currentPhase,
        BigDecimal totalBudget,
        BigDecimal estimatedSpending,
        BigDecimal actualSpending,
        BigDecimal remainingBudget,
        long blockedTaskCount,
        List<TaskSummary> blockedTasks,
        List<TaskSummary> upcomingTargetDates,
        List<TaskSummary> recentCompletedTasks,
        Map<PurchaseStatus, Long> purchaseStatusCounts,
        Map<LifecycleStatus, Long> deviceLifecycleCounts,
        Map<ManagedServiceStatus, Long> serviceStatusCounts,
        BackupCoverageWarningCounts backupCoverageWarnings,
        List<WarrantyExpirationSummary> upcomingWarrantyExpirations,
        List<RecommendedAction> nextRecommendedActions) {
}
