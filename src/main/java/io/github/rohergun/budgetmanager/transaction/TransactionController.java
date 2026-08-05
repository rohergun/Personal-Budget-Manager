package io.github.rohergun.budgetmanager.transaction;

import io.github.rohergun.budgetmanager.security.CustomUserDetails;
import io.github.rohergun.budgetmanager.transaction.dto.CreateTransactionRequest;
import io.github.rohergun.budgetmanager.transaction.dto.TransactionResponse;
import io.github.rohergun.budgetmanager.transaction.dto.TransactionUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {

        return ResponseEntity.ok(transactionService.getTransactionById(principal.getId(), id));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> listAll(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 20, sort = {"transactionDate"}, direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(transactionService.listAllByCurrentUser(principal.getId(), pageable));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid CreateTransactionRequest request) {

        TransactionResponse created = transactionService.addTransaction(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @RequestBody @Valid TransactionUpdateRequest request) {

        TransactionResponse updated = transactionService.updateTransaction(principal.getId(), id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {

        transactionService.deleteTransaction(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
