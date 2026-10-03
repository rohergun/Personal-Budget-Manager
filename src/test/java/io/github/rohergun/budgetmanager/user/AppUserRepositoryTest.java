package io.github.rohergun.budgetmanager.user;

import io.github.rohergun.budgetmanager.budget.Budget;
import io.github.rohergun.budgetmanager.category.Category;
import io.github.rohergun.budgetmanager.financialgoal.FinancialGoal;
import io.github.rohergun.budgetmanager.transaction.Transaction;
import io.github.rohergun.budgetmanager.transaction.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void deleteById_removesUserAndAllOwnedData() {
        AppUser user = entityManager.persist(AppUser.builder()
                .email("john.doe@example.com")
                .name("John")
                .surname("Doe")
                .password("encoded-password")
                .build());

        Category food = entityManager.persist(Category.builder()
                .name("Food")
                .user(user)
                .build());

        entityManager.persist(Budget.builder()
                .monthlyLimit(new BigDecimal("200.00"))
                .category(food)
                .user(user)
                .build());

        entityManager.persist(Transaction.builder()
                .amount(new BigDecimal("42.00"))
                .type(TransactionType.EXPENSE)
                .category(food)
                .user(user)
                .transactionDate(LocalDateTime.of(2026, 8, 10, 12, 0))
                .build());

        entityManager.persist(FinancialGoal.builder()
                .name("Holiday")
                .targetAmount(new BigDecimal("1000.00"))
                .currentAmount(BigDecimal.ZERO)
                .deadline(LocalDateTime.of(2027, 1, 1, 0, 0))
                .user(user)
                .build());

        UUID userId = user.getId();

        // Start from an empty persistence context, as a real DELETE /users/me request would.
        entityManager.flush();
        entityManager.clear();

        userRepository.deleteById(userId);
        entityManager.flush();

        assertThat(userRepository.findById(userId)).isEmpty();
        assertThat(countRows(Category.class)).isZero();
        assertThat(countRows(Budget.class)).isZero();
        assertThat(countRows(Transaction.class)).isZero();
        assertThat(countRows(FinancialGoal.class)).isZero();
    }

    private long countRows(Class<?> entity) {
        return entityManager.getEntityManager()
                .createQuery("select count(e) from " + entity.getSimpleName() + " e", Long.class)
                .getSingleResult();
    }
}
