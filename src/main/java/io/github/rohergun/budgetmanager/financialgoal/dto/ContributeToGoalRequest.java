package io.github.rohergun.budgetmanager.financialgoal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ContributeToGoalRequest(
        @NotNull @Positive
        BigDecimal amount
) { }
