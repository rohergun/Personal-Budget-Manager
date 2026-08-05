package io.github.rohergun.budgetmanager.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);
    Page<Transaction> findAllByUserId(UUID userId, Pageable pageable);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
