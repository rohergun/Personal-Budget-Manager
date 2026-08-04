package io.github.rohergun.budgetmanager.financialgoal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, UUID> {
    Optional<FinancialGoal> findByIdAndUserId(UUID id, UUID userId);
    Page<FinancialGoal> findAllByUserId(UUID userId, Pageable pageable);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
