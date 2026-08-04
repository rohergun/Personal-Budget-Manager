package io.github.rohergun.budgetmanager.financialgoal;

import io.github.rohergun.budgetmanager.financialgoal.dto.ContributeToGoalRequest;
import io.github.rohergun.budgetmanager.financialgoal.dto.CreateFinancialGoalRequest;
import io.github.rohergun.budgetmanager.financialgoal.dto.FinancialGoalResponse;
import io.github.rohergun.budgetmanager.financialgoal.dto.FinancialGoalUpdateRequest;
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
@RequestMapping("/api/v1/financial-goals")
@RequiredArgsConstructor
public class FinancialGoalController {

    private final FinancialGoalService goalService;

    @GetMapping("/{id}")
    public ResponseEntity<FinancialGoalResponse> getGoal(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {

        return ResponseEntity.ok(goalService.getGoalById(principal.getId(), id));
    }

    @GetMapping
    public ResponseEntity<Page<FinancialGoalResponse>> listAll(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 8, sort = {"deadline"}, direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(goalService.listAllByCurrentUser(principal.getId(), pageable));
    }

    @PostMapping
    public ResponseEntity<FinancialGoalResponse> createGoal(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid CreateFinancialGoalRequest request) {

        FinancialGoalResponse created = goalService.addGoal(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FinancialGoalResponse> updateGoal(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @RequestBody @Valid FinancialGoalUpdateRequest request) {

        FinancialGoalResponse updated = goalService.updateGoal(principal.getId(), id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/contributions")
    public ResponseEntity<FinancialGoalResponse> contributeToGoal(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @RequestBody @Valid ContributeToGoalRequest request) {

        FinancialGoalResponse updated = goalService.contributeToGoal(principal.getId(), id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {

        goalService.deleteGoal(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
