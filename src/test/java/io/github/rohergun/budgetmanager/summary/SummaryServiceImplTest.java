package io.github.rohergun.budgetmanager.summary;

import io.github.rohergun.budgetmanager.budget.Budget;
import io.github.rohergun.budgetmanager.budget.BudgetRepository;
import io.github.rohergun.budgetmanager.category.Category;
import io.github.rohergun.budgetmanager.summary.dto.CategorySpendingResponse;
import io.github.rohergun.budgetmanager.summary.dto.MonthlySummaryResponse;
import io.github.rohergun.budgetmanager.transaction.Transaction;
import io.github.rohergun.budgetmanager.transaction.TransactionRepository;
import io.github.rohergun.budgetmanager.transaction.TransactionType;
import io.github.rohergun.budgetmanager.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummaryServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private SummaryServiceImpl summaryService;

    private UUID userId;
    private AppUser user;
    private YearMonth month;

    private Category foodCategory;
    private Category transportCategory;
    private Category entertainmentCategory;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        month = YearMonth.of(2026, 8);

        user = AppUser.builder()
                .email("john.doe@example.com")
                .name("John")
                .surname("Doe")
                .password("encoded-password")
                .build();

        foodCategory = Category.builder()
                .name("Food")
                .description("Groceries and dining")
                .user(user)
                .build();
        foodCategory.setId(UUID.randomUUID());

        transportCategory = Category.builder()
                .name("Transport")
                .description("Bus, train, taxi")
                .user(user)
                .build();
        transportCategory.setId(UUID.randomUUID());

        entertainmentCategory = Category.builder()
                .name("Entertainment")
                .description("Movies, games")
                .user(user)
                .build();
        entertainmentCategory.setId(UUID.randomUUID());
    }

    private Transaction transaction(BigDecimal amount, TransactionType type, Category category, LocalDateTime date) {
        return Transaction.builder()
                .amount(amount)
                .type(type)
                .category(category)
                .user(user)
                .transactionDate(date)
                .build();
    }

    private Budget budget(BigDecimal limit, Category category) {
        return Budget.builder()
                .monthlyLimit(limit)
                .category(category)
                .user(user)
                .build();
    }

    @Test
    void getMonthlyTransactionsSummary_computesTotalsAndNet() {
        LocalDateTime withinMonth = month.atDay(10).atTime(12, 0);

        List<Transaction> transactions = List.of(
                transaction(new BigDecimal("1500.00"), TransactionType.INCOME, foodCategory, withinMonth),
                transaction(new BigDecimal("180.00"), TransactionType.EXPENSE, foodCategory, withinMonth),
                transaction(new BigDecimal("60.00"), TransactionType.EXPENSE, transportCategory, withinMonth)
        );

        when(transactionRepository.findAllByUserIdAndTransactionDateBetween(any(), any(), any()))
                .thenReturn(transactions);
        when(budgetRepository.findAllByUserId(userId)).thenReturn(List.of());

        MonthlySummaryResponse result = summaryService.getMonthlyTransactionsSummary(userId, month);

        assertThat(result.month()).isEqualTo(month);
        assertThat(result.totalIncome()).isEqualByComparingTo("1500.00");
        assertThat(result.totalExpenses()).isEqualByComparingTo("240.00");
        assertThat(result.net()).isEqualByComparingTo("1260.00");
    }

    @Test
    void getMonthlyTransactionsSummary_includesBudgetedCategoryWithZeroSpend() {
        LocalDateTime withinMonth = month.atDay(10).atTime(12, 0);

        List<Transaction> transactions = List.of(
                transaction(new BigDecimal("180.00"), TransactionType.EXPENSE, foodCategory, withinMonth)
        );
        List<Budget> budgets = List.of(
                budget(new BigDecimal("200.00"), foodCategory),
                budget(new BigDecimal("100.00"), entertainmentCategory) // no transactions this month
        );

        when(transactionRepository.findAllByUserIdAndTransactionDateBetween(any(), any(), any()))
                .thenReturn(transactions);
        when(budgetRepository.findAllByUserId(userId)).thenReturn(budgets);

        MonthlySummaryResponse result = summaryService.getMonthlyTransactionsSummary(userId, month);

        assertThat(result.byCategory())
                .extracting(CategorySpendingResponse::categoryName,
                        CategorySpendingResponse::spent,
                        CategorySpendingResponse::budgetLimit)
                .containsExactlyInAnyOrder(
                        tuple("Food", new BigDecimal("180.00"), new BigDecimal("200.00")),
                        tuple("Entertainment", BigDecimal.ZERO, new BigDecimal("100.00"))
                );
    }

    @Test
    void getMonthlyTransactionsSummary_includesSpentCategoryWithNoBudget() {
        LocalDateTime withinMonth = month.atDay(10).atTime(12, 0);

        List<Transaction> transactions = List.of(
                transaction(new BigDecimal("60.00"), TransactionType.EXPENSE, transportCategory, withinMonth)
        );

        when(transactionRepository.findAllByUserIdAndTransactionDateBetween(any(), any(), any()))
                .thenReturn(transactions);
        when(budgetRepository.findAllByUserId(userId)).thenReturn(List.of());

        MonthlySummaryResponse result = summaryService.getMonthlyTransactionsSummary(userId, month);

        assertThat(result.byCategory()).hasSize(1);
        CategorySpendingResponse transport = result.byCategory().get(0);
        assertThat(transport.categoryName()).isEqualTo("Transport");
        assertThat(transport.spent()).isEqualByComparingTo("60.00");
        assertThat(transport.budgetLimit()).isNull();
    }

    @Test
    void getMonthlyTransactionsSummary_unionsBudgetedAndSpentCategoriesWithoutDuplicates() {
        LocalDateTime withinMonth = month.atDay(10).atTime(12, 0);

        List<Transaction> transactions = List.of(
                transaction(new BigDecimal("180.00"), TransactionType.EXPENSE, foodCategory, withinMonth),
                transaction(new BigDecimal("60.00"), TransactionType.EXPENSE, transportCategory, withinMonth)
        );
        List<Budget> budgets = List.of(
                budget(new BigDecimal("200.00"), foodCategory),
                budget(new BigDecimal("100.00"), entertainmentCategory)
        );

        when(transactionRepository.findAllByUserIdAndTransactionDateBetween(any(), any(), any()))
                .thenReturn(transactions);
        when(budgetRepository.findAllByUserId(userId)).thenReturn(budgets);

        MonthlySummaryResponse result = summaryService.getMonthlyTransactionsSummary(userId, month);

        assertThat(result.byCategory())
                .extracting(CategorySpendingResponse::categoryName)
                .containsExactlyInAnyOrder("Food", "Transport", "Entertainment");

        assertThat(result.byCategory())
                .filteredOn(c -> c.categoryName().equals("Food"))
                .hasSize(1);
    }

    @Test
    void getMonthlyTransactionsSummary_excludesIncomeFromCategoryBreakdown() {
        LocalDateTime withinMonth = month.atDay(10).atTime(12, 0);

        List<Transaction> transactions = List.of(
                transaction(new BigDecimal("1500.00"), TransactionType.INCOME, foodCategory, withinMonth)
        );

        when(transactionRepository.findAllByUserIdAndTransactionDateBetween(any(), any(), any()))
                .thenReturn(transactions);
        when(budgetRepository.findAllByUserId(userId)).thenReturn(List.of());

        MonthlySummaryResponse result = summaryService.getMonthlyTransactionsSummary(userId, month);

        assertThat(result.byCategory()).isEmpty();
    }

    @Test
    void getMonthlyTransactionsSummary_returnsZeroTotalsAndEmptyBreakdown_whenNoActivity() {
        when(transactionRepository.findAllByUserIdAndTransactionDateBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(budgetRepository.findAllByUserId(userId)).thenReturn(List.of());

        MonthlySummaryResponse result = summaryService.getMonthlyTransactionsSummary(userId, month);

        assertThat(result.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.net()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.byCategory()).isEmpty();
    }
}