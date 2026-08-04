package io.github.rohergun.budgetmanager.financialgoal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FinancialGoalResponse(
        UUID id,
        String name,
        String description,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        LocalDateTime deadline,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
