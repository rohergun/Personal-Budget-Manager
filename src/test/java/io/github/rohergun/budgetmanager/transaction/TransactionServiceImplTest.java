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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
        verify(transactionRepository).findByIdAndUserId(transactionId, currentUserId);
    }

    @Test
    void getTransactionById_throwsTransactionNotFound_whenMissingOrNotOwned() {
        when(transactionRepository.findByIdAndUserId(transactionId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(currentUserId, transactionId))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.TRANSACTION_NOT_FOUND);

        verify(mapper, never()).toResponse(any());
    }

    @Test
    void listAllByCurrentUser_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 8);
        Page<Transaction> transactionPage = new PageImpl<>(List.of(existingTransaction), pageable, 1);
        TransactionResponse response = new TransactionResponse(
                transactionId, new BigDecimal("45.00"), TransactionType.EXPENSE,
                categoryId, "Food", LocalDateTime.now().minusDays(1), LocalDateTime.now(), LocalDateTime.now());

        when(transactionRepository.findAllByUserId(currentUserId, pageable)).thenReturn(transactionPage);
        when(mapper.toResponse(existingTransaction)).thenReturn(response);

        Page<TransactionResponse> result = transactionService.listAllByCurrentUser(currentUserId, pageable);

        assertThat(result.getContent()).containsExactly(response);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(transactionRepository).findAllByUserId(currentUserId, pageable);
    }

    @Test
    void addTransaction_savesAndReturnsMappedResponse_whenCategoryOwned() {
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
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("45.00");
        assertThat(captor.getValue().getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(captor.getValue().getTransactionDate()).isEqualTo(transactionDate);
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
    void updateTransaction_updatesFieldsAndReturnsMappedResponse_whenFoundOwnedAndCategoryOwned() {
        UUID newCategoryId = UUID.randomUUID();
        Category newCategory = Category.builder()
                .name("Transport")
                .description("Bus, train, taxi")
                .user(user)
                .build();
        LocalDateTime newDate = LocalDateTime.now().minusDays(3);

        TransactionUpdateRequest request = new TransactionUpdateRequest(
                new BigDecimal("60.00"), TransactionType.INCOME, newCategoryId, newDate);
        TransactionResponse expectedResponse = new TransactionResponse(
                transactionId, new BigDecimal("60.00"), TransactionType.INCOME,
                newCategoryId, "Transport", newDate, LocalDateTime.now(), LocalDateTime.now());

        when(transactionRepository.findByIdAndUserId(transactionId, currentUserId))
                .thenReturn(Optional.of(existingTransaction));
        when(categoryRepository.findByIdAndUserId(newCategoryId, currentUserId))
                .thenReturn(Optional.of(newCategory));
        when(mapper.toResponse(existingTransaction)).thenReturn(expectedResponse);

        TransactionResponse result = transactionService.updateTransaction(currentUserId, transactionId, request);

        assertThat(existingTransaction.getAmount()).isEqualByComparingTo("60.00");
        assertThat(existingTransaction.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(existingTransaction.getCategory()).isEqualTo(newCategory);
        assertThat(existingTransaction.getTransactionDate()).isEqualTo(newDate);
        assertThat(result).isEqualTo(expectedResponse);
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
    void deleteTransaction_deletesById_whenFoundAndOwned() {
        when(transactionRepository.existsByIdAndUserId(transactionId, currentUserId)).thenReturn(true);

        transactionService.deleteTransaction(currentUserId, transactionId);

        verify(transactionRepository).deleteById(transactionId);
    }

    @Test
    void deleteTransaction_throwsTransactionNotFound_whenMissingOrNotOwned() {
        when(transactionRepository.existsByIdAndUserId(transactionId, currentUserId)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.deleteTransaction(currentUserId, transactionId))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.TRANSACTION_NOT_FOUND);

        verify(transactionRepository, never()).deleteById(any());
    }
}
