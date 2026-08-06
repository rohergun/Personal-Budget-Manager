package io.github.rohergun.budgetmanager.summary;

import io.github.rohergun.budgetmanager.summary.dto.MonthlySummaryResponse;

import java.time.YearMonth;
import java.util.UUID;


public interface SummaryService {

    MonthlySummaryResponse getMonthlyTransactionsSummary(UUID userId, YearMonth month);
}
