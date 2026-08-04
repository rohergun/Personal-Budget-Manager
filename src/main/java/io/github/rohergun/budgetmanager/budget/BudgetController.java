package io.github.rohergun.budgetmanager.budget;

import io.github.rohergun.budgetmanager.budget.dto.BudgetResponse;
import io.github.rohergun.budgetmanager.budget.dto.BudgetUpdateRequest;
import io.github.rohergun.budgetmanager.budget.dto.CreateBudgetRequest;
import io.github.rohergun.budgetmanager.security.CustomUserDetails;
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
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudget(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {

        return ResponseEntity.ok(budgetService.getBudgetById(principal.getId(), id));
    }

    @GetMapping
    public ResponseEntity<Page<BudgetResponse>> listAll(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 8, sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(budgetService.listAllByCurrentUser(principal.getId(), pageable));
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<BudgetResponse> getBudgetByCategory(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID categoryId) {

        return ResponseEntity.ok(budgetService.getBudgetByCategory(principal.getId(), categoryId));
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid CreateBudgetRequest request) {

        BudgetResponse created = budgetService.addBudget(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @RequestBody @Valid BudgetUpdateRequest request) {

        BudgetResponse updated = budgetService.updateBudget(principal.getId(), id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {

        budgetService.deleteBudget(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
