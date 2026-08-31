package io.github.rohergun.budgetmanager.summary;

import io.github.rohergun.budgetmanager.budget.Budget;
import io.github.rohergun.budgetmanager.summary.dto.CategorySpendingResponse;
import io.github.rohergun.budgetmanager.transaction.Transaction;
import io.github.rohergun.budgetmanager.transaction.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;



@Component
public class SummaryAggregator {

    public BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<CategorySpendingResponse> buildCategoryBreakdown(Set<UUID> allCategoryIds,
                                                                  Map<UUID, String> categoryNames,
                                                                  Map<UUID, BigDecimal> spentByCategory,
                                                                  Map<UUID, BigDecimal> budgetLimitsByCategory) {
        return allCategoryIds.stream()
                .map(categoryId -> new CategorySpendingResponse(
                        categoryId,
                        categoryNames.get(categoryId),
                        spentByCategory.getOrDefault(categoryId, BigDecimal.ZERO),
                        budgetLimitsByCategory.get(categoryId)
                ))
                .toList();
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
