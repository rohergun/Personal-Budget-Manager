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
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AppUserRepository userRepository;
    private final TransactionMapper mapper;

    private final CacheManager cacheManager;

    @Override
    public TransactionResponse getTransactionById(UUID userId, UUID transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.TRANSACTION_NOT_FOUND));

        return mapper.toResponse(transaction);
    }

    @Override
    public Page<TransactionResponse> listAllByCurrentUser(UUID userId, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findAllByUserId(userId, pageable);
        return transactions.map(mapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = "monthlySummary",
            key = "#userId + '-' + T(java.time.YearMonth).from(#request.transactionDate())"
    )
    public TransactionResponse addTransaction(UUID userId, CreateTransactionRequest request) {
        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.CATEGORY_NOT_FOUND));

        AppUser user = userRepository.getReferenceById(userId);

        Transaction newTransaction = Transaction.builder()
                .amount(request.amount())
                .type(request.type())
                .category(category)
                .user(user)
                .transactionDate(request.transactionDate())
                .build();

        Transaction saved = transactionRepository.save(newTransaction);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(UUID userId, UUID transactionId, TransactionUpdateRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.TRANSACTION_NOT_FOUND));

        YearMonth oldMonth = YearMonth.from(transaction.getTransactionDate());
        YearMonth newMonth = YearMonth.from(request.transactionDate());

        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.CATEGORY_NOT_FOUND));

        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setCategory(category);
        transaction.setTransactionDate(request.transactionDate());

        evictSummaryCache(userId, oldMonth);
        if (!oldMonth.equals(newMonth)) {
            evictSummaryCache(userId, newMonth);
        }

        return mapper.toResponse(transaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(UUID userId, UUID transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.TRANSACTION_NOT_FOUND));

        YearMonth month = YearMonth.from(transaction.getTransactionDate());

        transaction.setDeletedAt(LocalDateTime.now());
        evictSummaryCache(userId, month);
    }

    private void evictSummaryCache(UUID userId, YearMonth month) {
        var cache = cacheManager.getCache("monthlySummary");
        if (cache != null) {
            cache.evict(userId + "-" + month);
        }
    }
}