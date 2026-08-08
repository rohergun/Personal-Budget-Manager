package io.github.rohergun.budgetmanager.budget;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    Optional<Budget> findByIdAndUserId(UUID id, UUID userId);
    Page<Budget> findAllByUserId(UUID userId, Pageable pageable);
    Optional<Budget> findByUserIdAndCategoryId(UUID userId, UUID categoryId);
    boolean existsByUserIdAndCategoryId(UUID userId, UUID categoryId);
    boolean existsByUserIdAndCategoryIdAndIdNot(UUID userId, UUID categoryId, UUID id);
    boolean existsByIdAndUserId(UUID id, UUID userId);
    List<Budget> findAllByUserId(UUID userId);
}
