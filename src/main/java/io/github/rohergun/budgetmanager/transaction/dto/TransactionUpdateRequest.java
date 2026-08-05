package io.github.rohergun.budgetmanager.transaction.dto;

import io.github.rohergun.budgetmanager.transaction.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionUpdateRequest(
        @NotNull @Positive
        BigDecimal amount,

        @NotNull
        TransactionType type,

        @NotNull
        UUID categoryId,

        @NotNull
        LocalDateTime transactionDate
) { }