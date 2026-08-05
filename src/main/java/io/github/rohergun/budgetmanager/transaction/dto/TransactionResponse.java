package io.github.rohergun.budgetmanager.transaction.dto;

import io.github.rohergun.budgetmanager.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse (
        UUID id,
        BigDecimal amount,
        TransactionType type,
        UUID categoryId,
        String categoryName,
        LocalDateTime transactionDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){ }
