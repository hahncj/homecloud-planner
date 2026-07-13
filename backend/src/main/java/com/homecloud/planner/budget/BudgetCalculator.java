package com.homecloud.planner.budget;

import com.homecloud.planner.shopping.PurchaseItem;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Computes budget figures on the fly from the current purchase-item list
 * rather than persisting them, so they always reflect the latest prices and
 * statuses.
 */
@Component
public class BudgetCalculator {

    public BudgetSummary summarize(BigDecimal projectBudget, List<PurchaseItem> items) {
        List<PurchaseItem> committed = items.stream().filter(PurchaseItem::isCommitted).toList();

        BigDecimal estimatedTotal = sum(committed, PurchaseItem::getEstimatedTotal);
        BigDecimal actualTotal = sum(committed, PurchaseItem::getActualTotal);
        BigDecimal committedSpending = committed.stream()
                .map(this::committedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingBudget = projectBudget == null ? null : projectBudget.subtract(committedSpending);

        List<CategorySummary> categories = committed.stream()
                .collect(Collectors.groupingBy(PurchaseItem::getCategory))
                .entrySet()
                .stream()
                .map(entry -> new CategorySummary(
                        entry.getKey(),
                        entry.getValue().size(),
                        sum(entry.getValue(), PurchaseItem::getEstimatedTotal),
                        sum(entry.getValue(), PurchaseItem::getActualTotal),
                        entry.getValue().stream().map(this::committedAmount).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .sorted(Comparator.comparing(CategorySummary::category))
                .toList();

        return new BudgetSummary(projectBudget, estimatedTotal, actualTotal, committedSpending, remainingBudget, categories);
    }

    /** Actual cost when known, otherwise the estimate; zero when neither is known. */
    private BigDecimal committedAmount(PurchaseItem item) {
        if (item.getActualTotal() != null) {
            return item.getActualTotal();
        }
        return item.getEstimatedTotal() != null ? item.getEstimatedTotal() : BigDecimal.ZERO;
    }

    private BigDecimal sum(List<PurchaseItem> items, Function<PurchaseItem, BigDecimal> extractor) {
        return items.stream()
                .map(extractor)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
