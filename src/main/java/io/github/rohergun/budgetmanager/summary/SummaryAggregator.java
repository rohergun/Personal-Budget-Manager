package io.github.rohergun.budgetmanager.summary;

import io.github.rohergun.budgetmanager.budget.Budget;
import io.github.rohergun.budgetmanager.summary.dto.CategorySpendingResponse;
import io.github.rohergun.budgetmanager.transaction.Transaction;
import io.github.rohergun.budgetmanager.transaction.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;



@Component
public class SummaryAggregator {


    public List<CategorySpendingResponse> buildCategoryBreakdown(List<Transaction> transactions, List<Budget> budgets){

        Map<UUID, BigDecimal> spentByCategory = extractMonthlyExpensePerCategory(transactions);
        Map<UUID, String> categoryNames = extractCategoryNamesFromTransactions(transactions);
        Map<UUID, BigDecimal> budgetLimitsByCategory = extractBudgetLimitsPerCategory(budgets);

        budgets.forEach(budget ->
                categoryNames.putIfAbsent(budget.getCategory().getId(), budget.getCategory().getName()));

        Set<UUID> allCategoryIds = new HashSet<>();
        allCategoryIds.addAll(budgetLimitsByCategory.keySet());
        allCategoryIds.addAll(spentByCategory.keySet());

        return allCategoryIds.stream()
                .map(categoryId -> new CategorySpendingResponse(
                        categoryId,
                        categoryNames.get(categoryId),
                        spentByCategory.getOrDefault(categoryId, BigDecimal.ZERO),
                        budgetLimitsByCategory.get(categoryId)
                ))
                .toList();
    }

    public BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<UUID, BigDecimal> extractMonthlyExpensePerCategory(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }

    public Map<UUID, String> extractCategoryNamesFromTransactions(List<Transaction> transactions) {
        return  transactions.stream()
                .collect(Collectors.toMap(
                        t -> t.getCategory().getId(),
                        t -> t.getCategory().getName(),
                        (existing, replacement) -> existing
                ));
    }

    public Map<UUID, BigDecimal> extractBudgetLimitsPerCategory(List<Budget> budgets) {
        return budgets.stream()
                .collect(Collectors.toMap(
                        budget -> budget.getCategory().getId(),
                        Budget::getMonthlyLimit
                ));
    }

}
