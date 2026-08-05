package io.github.rohergun.budgetmanager.transaction;

import io.github.rohergun.budgetmanager.transaction.dto.CreateTransactionRequest;
import io.github.rohergun.budgetmanager.transaction.dto.TransactionResponse;
import io.github.rohergun.budgetmanager.transaction.dto.TransactionUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransactionService {
    TransactionResponse getTransactionById(UUID userId, UUID transactionId);
    Page<TransactionResponse> listAllByCurrentUser(UUID userId, Pageable pageable);
    TransactionResponse addTransaction(UUID userId, CreateTransactionRequest request);
    TransactionResponse updateTransaction(UUID userId, UUID transactionId, TransactionUpdateRequest request);
    void deleteTransaction(UUID userId, UUID transactionId);
}