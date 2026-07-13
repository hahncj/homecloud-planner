package com.homecloud.planner.budget;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/budget")
class BudgetController {

    private final BudgetService budgetService;

    BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    BudgetSummary getBudgetSummary(@PathVariable UUID projectId) {
        return budgetService.getBudgetSummary(projectId);
    }
}
