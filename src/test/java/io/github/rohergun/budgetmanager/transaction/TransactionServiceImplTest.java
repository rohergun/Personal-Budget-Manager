package io.github.rohergun.budgetmanager.transaction;

import io.github.rohergun.budgetmanager.category.Category;
import io.github.rohergun.budgetmanager.category.CategoryRepository;
import io.github.rohergun.budgetmanager.exception.BudgetManagerException;
import io.github.rohergun.budgetmanager.exception.DomainErrorMessage;
import io.github.rohergun.budgetmanager.transaction.dto.CreateTransactionRequest;
import io.github.rohergun.budgetmanager.transaction.dto.TransactionResponse;
import io.github.rohergun.budgetmanager.transaction.dto.TransactionUpdateRequest;
import io.github.rohergun.budgetmanager.user.AppUser;
import io.github.rohergun.budgetmanager.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private TransactionMapper mapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private UUID currentUserId;
    private UUID transactionId;
    private UUID categoryId;
    private AppUser user;
    private Category category;
    private Transaction existingTransaction;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        user = AppUser.builder()
                .email("john.doe@example.com")
                .name("John")
                .surname("Doe")
                .password("encoded-password")
                .build();

        category = Category.builder()
                .name("Food")
                .description("Groceries and dining")
                .user(user)
                .build();

        existingTransaction = Transaction.builder()
                .amount(new BigDecimal("45.00"))
                .type(TransactionType.EXPENSE)
                .category(category)
                .user(user)
                .transactionDate(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void getTransactionById_returnsMappedResponse_whenFoundAndOwned() {
        TransactionResponse expectedResponse = new TransactionResponse(
                transactionId, new BigDecimal("45.00"), TransactionType.EXPENSE,
                categoryId, "Food", LocalDateTime.now().minusDays(1), LocalDateTime.now(), LocalDateTime.now());

        when(transactionRepository.findByIdAndUserId(transactionId, currentUserId))
                .thenReturn(Optional.of(existingTransaction));
        when(mapper.toResponse(existingTransaction)).thenReturn(expectedResponse);

        TransactionResponse result = transactionService.getTransactionById(currentUserId, transactionId);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getTransactionById_throwsTransactionNotFound_whenMissingOrNotOwned() {
        when(transactionRepository.findByIdAndUserId(transactionId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(currentUserId, transactionId))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.TRANSACTION_NOT_FOUND);
    }

    @Test
    void addTransaction_savesAndReturnsMappedResponse_whenCategoryOwned() {
        // No cache mocking needed here: @CacheEvict is annotation-based and only
        // applies through Spring's AOP proxy — inert in a plain Mockito unit test.
        LocalDateTime transactionDate = LocalDateTime.now().minusDays(2);
        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("45.00"), TransactionType.EXPENSE, categoryId, transactionDate);
        TransactionResponse expectedResponse = new TransactionResponse(
                transactionId, new BigDecimal("45.00"), TransactionType.EXPENSE,
                categoryId, "Food", transactionDate, LocalDateTime.now(), LocalDateTime.now());

        when(categoryRepository.findByIdAndUserId(categoryId, currentUserId))
                .thenReturn(Optional.of(category));
        when(userRepository.getReferenceById(currentUserId)).thenReturn(user);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Transaction.class))).thenReturn(expectedResponse);

        TransactionResponse result = transactionService.addTransaction(currentUserId, request);

        assertThat(result).isEqualTo(expectedResponse);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getCategory()).isEqualTo(category);
    }

    @Test
    void addTransaction_throwsCategoryNotFound_whenCategoryMissingOrNotOwned() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("45.00"), TransactionType.EXPENSE, categoryId, LocalDateTime.now());

        when(categoryRepository.findByIdAndUserId(categoryId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.addTransaction(currentUserId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.CATEGORY_NOT_FOUND);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void updateTransaction_updatesFieldsAndEvictsSameMonthCache_whenMonthUnchanged() {
        LocalDateTime sameMonthNewDate = existingTransaction.getTransactionDate().plusDays(1);
        var oldMonth = java.time.YearMonth.from(existingTransaction.getTransactionDate());

        TransactionUpdateRequest request = new TransactionUpdateRequest(
                new BigDecimal("60.00"), TransactionType.INCOME, categoryId, sameMonthNewDate);
        TransactionResponse expectedResponse = new TransactionResponse(
                transactionId, new BigDecimal("60.00"), TransactionType.INCOME,
                categoryId, "Food", sameMonthNewDate, LocalDateTime.now(), LocalDateTime.now());

        when(transactionRepository.findByIdAndUserId(transactionId, currentUserId))
                .thenReturn(Optional.of(existingTransaction));
        when(categoryRepository.findByIdAndUserId(categoryId, currentUserId))
                .thenReturn(Optional.of(category));
        when(cacheManager.getCache("monthlySummary")).thenReturn(cache);
        when(mapper.toResponse(existingTransaction)).thenReturn(expectedResponse);

        TransactionResponse result = transactionService.updateTransaction(currentUserId, transactionId, request);

        assertThat(result).isEqualTo(expectedResponse);
        // same month before and after → exactly one eviction
        verify(cache, times(1)).evict(currentUserId + "-" + oldMonth);
    }

    @Test
    void updateTransaction_evictsBothMonths_whenMonthChanges() {
        var oldMonth = java.time.YearMonth.from(existingTransaction.getTransactionDate());
        LocalDateTime differentMonthDate = existingTransaction.getTransactionDate().plusMonths(1);
        var newMonth = java.time.YearMonth.from(differentMonthDate);

        TransactionUpdateRequest request = new TransactionUpdateRequest(
                new BigDecimal("60.00"), TransactionType.INCOME, categoryId, differentMonthDate);
        TransactionResponse expectedResponse = new TransactionResponse(
                transactionId, new BigDecimal("60.00"), TransactionType.INCOME,
                categoryId, "Food", differentMonthDate, LocalDateTime.now(), LocalDateTime.now());

        when(transactionRepository.findByIdAndUserId(transactionId, currentUserId))
                .thenReturn(Optional.of(existingTransaction));
        when(categoryRepository.findByIdAndUserId(categoryId, currentUserId))
                .thenReturn(Optional.of(category));
        when(cacheManager.getCache("monthlySummary")).thenReturn(cache);
        when(mapper.toResponse(existingTransaction)).thenReturn(expectedResponse);

        transactionService.updateTransaction(currentUserId, transactionId, request);

        verify(cache).evict(currentUserId + "-" + oldMonth);
        verify(cache).evict(currentUserId + "-" + newMonth);
    }

    @Test
    void updateTransaction_throwsTransactionNotFound_whenMissingOrNotOwned() {
        TransactionUpdateRequest request = new TransactionUpdateRequest(
                new BigDecimal("60.00"), TransactionType.INCOME, categoryId, LocalDateTime.now());

        when(transactionRepository.findByIdAndUserId(transactionId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.updateTransaction(currentUserId, transactionId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.TRANSACTION_NOT_FOUND);

        verify(categoryRepository, never()).findByIdAndUserId(any(), any());
    }

    @Test
    void updateTransaction_throwsCategoryNotFound_whenNewCategoryMissingOrNotOwned() {
        TransactionUpdateRequest request = new TransactionUpdateRequest(
                new BigDecimal("60.00"), TransactionType.INCOME, categoryId, LocalDateTime.now());

        when(transactionRepository.findByIdAndUserId(transactionId, currentUserId))
                .thenReturn(Optional.of(existingTransaction));
        when(categoryRepository.findByIdAndUserId(categoryId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.updateTransaction(currentUserId, transactionId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.CATEGORY_NOT_FOUND);
    }

    @Test
    void deleteTransaction_deletesAndEvictsThatMonthsCache_whenFoundAndOwned() {
        var month = java.time.YearMonth.from(existingTransaction.getTransactionDate());

        when(transactionRepository.findByIdAndUserId(transactionId, currentUserId))
                .thenReturn(Optional.of(existingTransaction));
        when(cacheManager.getCache("monthlySummary")).thenReturn(cache);

        transactionService.deleteTransaction(currentUserId, transactionId);

        assertThat(existingTransaction.getDeletedAt()).isNotNull();
        verify(transactionRepository, never()).deleteById(any());
        verify(cache).evict(currentUserId + "-" + month);
    }

    @Test
    void deleteTransaction_throwsTransactionNotFound_whenMissingOrNotOwned() {
        when(transactionRepository.findByIdAndUserId(transactionId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(currentUserId, transactionId))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.TRANSACTION_NOT_FOUND);

        verify(transactionRepository, never()).deleteById(any());
    }
}