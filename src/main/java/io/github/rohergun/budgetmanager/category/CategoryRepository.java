package io.github.rohergun.budgetmanager.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByIdAndUserId(UUID id, UUID userId);
    Page<Category> findAllByUserId(UUID userId, Pageable pageable);
    boolean existsByUserIdAndName(UUID userId, String name);
    boolean existsByIdAndUserId(UUID id, UUID userId);
    boolean existsByUserIdAndNameAndIdNot(UUID userId, String name, UUID id);
}
