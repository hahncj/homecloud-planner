package com.homecloud.planner.budget;

import static org.assertj.core.api.Assertions.assertThat;

import com.homecloud.planner.project.Project;
import com.homecloud.planner.project.ProjectStatus;
import com.homecloud.planner.shopping.PurchaseItem;
import com.homecloud.planner.shopping.PurchaseStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class BudgetCalculatorTest {

    private final BudgetCalculator calculator = new BudgetCalculator();

    private static Project project() {
        Project project = new Project("Personal Hybrid Cloud", null, ProjectStatus.IN_PROGRESS, null, null, null);
        ReflectionTestUtils.setField(project, "id", UUID.randomUUID());
        return project;
    }

    private static PurchaseItem item(
            Project project,
            String category,
            int quantity,
            BigDecimal estimatedUnitPrice,
            BigDecimal actualUnitPrice,
            PurchaseStatus status) {
        PurchaseItem item = new PurchaseItem(
                project, null, category, "Item", null, null, null, quantity, estimatedUnitPrice, actualUnitPrice,
                null, null, status, null, null, null, null, null);
        ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
        return item;
    }

    @Test
    void committedSpendingPrefersActualOverEstimatedTotal() {
        Project project = project();
        PurchaseItem estimatedOnly = item(
                project, "Networking", 2, new BigDecimal("10.00"), null, PurchaseStatus.PLANNED);
        PurchaseItem withActual = item(
                project, "Networking", 1, new BigDecimal("50.00"), new BigDecimal("45.00"), PurchaseStatus.RECEIVED);

        BudgetSummary summary = calculator.summarize(null, List.of(estimatedOnly, withActual));

        // estimatedOnly contributes 20.00 (no actual yet); withActual contributes 45.00 (actual wins).
        assertThat(summary.committedSpending()).isEqualByComparingTo("65.00");
    }

    @Test
    void cancelledItemsAreExcludedFromCommittedSpending() {
        Project project = project();
        PurchaseItem cancelled = item(
                project, "Networking", 3, new BigDecimal("100.00"), null, PurchaseStatus.CANCELLED);
        PurchaseItem active = item(project, "Networking", 1, new BigDecimal("20.00"), null, PurchaseStatus.IDEA);

        BudgetSummary summary = calculator.summarize(null, List.of(cancelled, active));

        assertThat(summary.committedSpending()).isEqualByComparingTo("20.00");
        assertThat(summary.estimatedTotal()).isEqualByComparingTo("20.00");
    }

    @Test
    void remainingBudgetIsBudgetMinusCommittedSpending() {
        Project project = project();
        PurchaseItem active = item(
                project, "Storage", 1, new BigDecimal("200.00"), new BigDecimal("180.00"), PurchaseStatus.RECEIVED);

        BudgetSummary summary = calculator.summarize(new BigDecimal("1000.00"), List.of(active));

        assertThat(summary.remainingBudget()).isEqualByComparingTo("820.00");
    }

    @Test
    void remainingBudgetIsNullWhenProjectHasNoBudget() {
        BudgetSummary summary = calculator.summarize(null, List.of());

        assertThat(summary.remainingBudget()).isNull();
        assertThat(summary.committedSpending()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void categorySummariesGroupAndTotalByCategory() {
        Project project = project();
        PurchaseItem networking = item(
                project, "Networking", 1, new BigDecimal("30.00"), null, PurchaseStatus.PLANNED);
        PurchaseItem storageA = item(
                project, "Storage", 2, new BigDecimal("50.00"), new BigDecimal("55.00"), PurchaseStatus.RECEIVED);
        PurchaseItem storageB = item(
                project, "Storage", 1, new BigDecimal("10.00"), null, PurchaseStatus.IDEA);

        BudgetSummary summary = calculator.summarize(null, List.of(networking, storageA, storageB));

        assertThat(summary.categories()).hasSize(2);
        CategorySummary storage = summary.categories().stream()
                .filter(category -> category.category().equals("Storage"))
                .findFirst()
                .orElseThrow();
        assertThat(storage.itemCount()).isEqualTo(2);
        assertThat(storage.estimatedTotal()).isEqualByComparingTo("110.00");
        assertThat(storage.actualTotal()).isEqualByComparingTo("110.00");
        assertThat(storage.committedSpending()).isEqualByComparingTo("120.00");
    }
}
