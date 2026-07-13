package com.homecloud.planner.budget;

import java.math.BigDecimal;

public record CategorySummary(
        String category, long itemCount, BigDecimal estimatedTotal, BigDecimal actualTotal, BigDecimal committedSpending) {
}
