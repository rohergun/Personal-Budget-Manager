package io.github.rohergun.budgetmanager.summary;

import io.github.rohergun.budgetmanager.budget.Budget;
import io.github.rohergun.budgetmanager.budget.BudgetRepository;
import io.github.rohergun.budgetmanager.summary.dto.CategorySpendingResponse;
import io.github.rohergun.budgetmanager.summary.dto.MonthlySummaryResponse;
import io.github.rohergun.budgetmanager.transaction.Transaction;
import io.github.rohergun.budgetmanager.transaction.TransactionRepository;
import io.github.rohergun.budgetmanager.transaction.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    @Override
    public MonthlySummaryResponse getMonthlyTransactionsSummary(UUID userId, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);

        List<Transaction> transactions =
                transactionRepository.findAllByUserIdAndTransactionDateBetween(userId, start, end);

        BigDecimal totalIncome = sumByType(transactions, TransactionType.INCOME);
        BigDecimal totalExpenses = sumByType(transactions, TransactionType.EXPENSE);
        BigDecimal net = totalIncome.subtract(totalExpenses);

        // How much was actually spent per category this month (expenses only)
        Map<UUID, BigDecimal> spentByCategory = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        // Category names, sourced from transactions first
        Map<UUID, String> categoryNames = transactions.stream()
                .collect(Collectors.toMap(
                        t -> t.getCategory().getId(),
                        t -> t.getCategory().getName(),
                        (existing, replacement) -> existing
                ));

        // Every budget this user has, keyed by category — the "must always appear" set
        List<Budget> budgets = budgetRepository.findAllByUserId(userId);
        Map<UUID, BigDecimal> budgetLimitsByCategory = budgets.stream()
                .collect(Collectors.toMap(
                        budget -> budget.getCategory().getId(),
                        Budget::getMonthlyLimit
                ));
        budgets.forEach(budget ->
                categoryNames.putIfAbsent(budget.getCategory().getId(), budget.getCategory().getName()));

        // Union of categories that were budgeted OR spent-on this month
        Set<UUID> allCategoryIds = new HashSet<>();
        allCategoryIds.addAll(budgetLimitsByCategory.keySet());
        allCategoryIds.addAll(spentByCategory.keySet());

        List<CategorySpendingResponse> byCategory = allCategoryIds.stream()
                .map(categoryId -> new CategorySpendingResponse(
                        categoryId,
                        categoryNames.get(categoryId),
                        spentByCategory.getOrDefault(categoryId, BigDecimal.ZERO),
                        budgetLimitsByCategory.get(categoryId) // null if no budget exists
                ))
                .toList();

        return new MonthlySummaryResponse(month, totalIncome, totalExpenses, net, byCategory);
    }

    private BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
