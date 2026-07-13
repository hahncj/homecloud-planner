package com.homecloud.planner.budget;

import java.math.BigDecimal;
import java.util.List;

public record BudgetSummary(
        BigDecimal budget,
        BigDecimal estimatedTotal,
        BigDecimal actualTotal,
        BigDecimal committedSpending,
        BigDecimal remainingBudget,
        List<CategorySummary> categories) {
}
