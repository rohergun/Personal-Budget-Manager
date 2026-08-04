package io.github.rohergun.budgetmanager.budget;

import io.github.rohergun.budgetmanager.budget.dto.BudgetResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BudgetMapper {
    BudgetResponse toResponse(Budget budget);
}
