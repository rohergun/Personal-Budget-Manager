package io.github.rohergun.budgetmanager.summary.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record MonthlySummaryResponse(
        YearMonth month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal net,
        List<CategorySpendingResponse> byCategory
) { }
