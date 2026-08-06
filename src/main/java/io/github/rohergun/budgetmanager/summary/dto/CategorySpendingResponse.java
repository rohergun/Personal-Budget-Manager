package io.github.rohergun.budgetmanager.summary.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CategorySpendingResponse(
        UUID categoryId,
        String categoryName,
        BigDecimal spent,
        BigDecimal budgetLimit
) { }
