package io.github.rohergun.budgetmanager.category;

import io.github.rohergun.budgetmanager.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByUserAndName(AppUser user, String name);
}
