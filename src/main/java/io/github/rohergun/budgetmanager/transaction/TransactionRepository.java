package io.github.rohergun.budgetmanager.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    @Query(
            value = "select t from Transaction t join fetch t.category where t.user.id = :userId",
            countQuery = "select count(t) from Transaction t where t.user.id = :userId"
    )
    Page<Transaction> findAllByUserId(UUID userId, Pageable pageable);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("select t from Transaction t join fetch t.category " +
            "where t.user.id = :userId and t.transactionDate between :start and :end")
    List<Transaction> findAllByUserIdAndTransactionDateBetween(
            UUID userId, LocalDateTime start, LocalDateTime end);

}
