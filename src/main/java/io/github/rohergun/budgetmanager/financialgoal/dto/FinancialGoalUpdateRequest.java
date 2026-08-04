package io.github.rohergun.budgetmanager.financialgoal.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FinancialGoalUpdateRequest(
        @NotBlank @Size(min = 2, max = 50)
        String name,

        @Size(max = 255)
        String description,

        @NotNull @Positive
        BigDecimal targetAmount,

        @NotNull @Future
        LocalDateTime deadline
) { }
