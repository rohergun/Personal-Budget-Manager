package io.github.rohergun.budgetmanager.summary;

import io.github.rohergun.budgetmanager.budget.Budget;
import io.github.rohergun.budgetmanager.budget.BudgetRepository;
import io.github.rohergun.budgetmanager.summary.dto.CategorySpendingResponse;
import io.github.rohergun.budgetmanager.summary.dto.MonthlySummaryResponse;
import io.github.rohergun.budgetmanager.transaction.Transaction;
import io.github.rohergun.budgetmanager.transaction.TransactionRepository;
import io.github.rohergun.budgetmanager.transaction.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService{

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final SummaryAggregator summaryAggregator;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "monthlySummary", key = "#userId + '-' + #month")
    public MonthlySummaryResponse getMonthlyTransactionsSummary(UUID userId, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);

        List<Transaction> transactions =
                transactionRepository.findAllByUserIdAndTransactionDateBetween(userId, start, end);

        BigDecimal totalIncome = summaryAggregator.sumByType(transactions, TransactionType.INCOME);
        BigDecimal totalExpenses = summaryAggregator.sumByType(transactions, TransactionType.EXPENSE);
        BigDecimal net = totalIncome.subtract(totalExpenses);

        List<Budget> budgets = budgetRepository.findAllByUserId(userId);


        List<CategorySpendingResponse> byCategory = summaryAggregator.buildCategoryBreakdown(transactions, budgets);

        return new MonthlySummaryResponse(month, totalIncome, totalExpenses, net, byCategory);
    }

}
