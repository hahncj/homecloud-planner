package com.homecloud.planner.budget;

import com.homecloud.planner.common.NotFoundException;
import com.homecloud.planner.project.Project;
import com.homecloud.planner.project.ProjectRepository;
import com.homecloud.planner.shopping.PurchaseItemRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BudgetService {

    private final ProjectRepository projectRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final BudgetCalculator budgetCalculator;

    public BudgetService(
            ProjectRepository projectRepository,
            PurchaseItemRepository purchaseItemRepository,
            BudgetCalculator budgetCalculator) {
        this.projectRepository = projectRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.budgetCalculator = budgetCalculator;
    }

    @Transactional(readOnly = true)
    public BudgetSummary getBudgetSummary(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project " + projectId + " was not found."));
        return budgetCalculator.summarize(project.getBudget(), purchaseItemRepository.findByProjectId(projectId));
    }
}
