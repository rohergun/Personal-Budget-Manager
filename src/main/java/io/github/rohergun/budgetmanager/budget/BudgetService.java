package io.github.rohergun.budgetmanager.budget;

import io.github.rohergun.budgetmanager.budget.dto.BudgetResponse;
import io.github.rohergun.budgetmanager.budget.dto.BudgetUpdateRequest;
import io.github.rohergun.budgetmanager.budget.dto.CreateBudgetRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BudgetService {
    BudgetResponse getBudgetById(UUID userId, UUID budgetId);
    Page<BudgetResponse> listAllByCurrentUser(UUID userId, Pageable pageable);
    BudgetResponse getBudgetByCategory(UUID userId, UUID categoryId);
    BudgetResponse addBudget(UUID userId, CreateBudgetRequest request);
    BudgetResponse updateBudget(UUID userId, UUID budgetId, BudgetUpdateRequest request);
    void deleteBudget(UUID userId, UUID budgetId);
}
