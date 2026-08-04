package io.github.rohergun.budgetmanager.budget.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateBudgetRequest(
        @NotNull @Positive
        BigDecimal monthlyLimit,

        @NotNull
        UUID categoryId
) { }