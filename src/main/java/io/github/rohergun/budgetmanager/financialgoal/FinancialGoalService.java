package io.github.rohergun.budgetmanager.financialgoal;

import io.github.rohergun.budgetmanager.financialgoal.dto.ContributeToGoalRequest;
import io.github.rohergun.budgetmanager.financialgoal.dto.CreateFinancialGoalRequest;
import io.github.rohergun.budgetmanager.financialgoal.dto.FinancialGoalResponse;
import io.github.rohergun.budgetmanager.financialgoal.dto.FinancialGoalUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FinancialGoalService {
    FinancialGoalResponse getGoalById(UUID userId, UUID goalId);
    Page<FinancialGoalResponse> listAllByCurrentUser(UUID userId, Pageable pageable);
    FinancialGoalResponse addGoal(UUID userId, CreateFinancialGoalRequest request);
    FinancialGoalResponse updateGoal(UUID userId, UUID goalId, FinancialGoalUpdateRequest request);
    FinancialGoalResponse contributeToGoal(UUID userId, UUID goalId, ContributeToGoalRequest request);
    void deleteGoal(UUID userId, UUID goalId);
}
