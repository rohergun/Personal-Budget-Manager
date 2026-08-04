package io.github.rohergun.budgetmanager.financialgoal;

import io.github.rohergun.budgetmanager.financialgoal.dto.FinancialGoalResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FinancialGoalMapper {
    FinancialGoalResponse toResponse(FinancialGoal financialGoal);
}
