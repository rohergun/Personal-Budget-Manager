package io.github.rohergun.budgetmanager.transaction;

import io.github.rohergun.budgetmanager.category.Category;
import io.github.rohergun.budgetmanager.user.AppUser;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private AppUser user;
    private Category foodCategory;

    @BeforeEach
    void setUp() {
        user = entityManager.persist(AppUser.builder()
                .email("john.doe@example.com")
                .name("John")
                .surname("Doe")
                .password("encoded-password")
                .build());

        foodCategory = entityManager.persist(Category.builder()
                .name("Food")
                .user(user)
                .build());
    }

    private Transaction persistTransaction(Category category) {
        Transaction transaction = entityManager.persist(Transaction.builder()
                .amount(new BigDecimal("42.00"))
                .type(TransactionType.EXPENSE)
                .category(category)
                .user(user)
                .transactionDate(LocalDateTime.of(2026, 8, 10, 12, 0))
                .build());

        // Detach everything so the query below has to load from the database,
        // just like a fresh request with open-in-view disabled.
        entityManager.flush();
        entityManager.clear();
        return transaction;
    }

    @Test
    void findByIdAndUserId_fetchesCategoryEagerly() {
        Transaction saved = persistTransaction(foodCategory);

        Optional<Transaction> found = transactionRepository.findByIdAndUserId(saved.getId(), user.getId());

        assertThat(found).isPresent();
        assertThat(Hibernate.isInitialized(found.get().getCategory())).isTrue();
        assertThat(found.get().getCategory().getName()).isEqualTo("Food");
    }

    @Test
    void findByIdAndUserId_returnsTransactionWithoutCategory() {
        Transaction saved = persistTransaction(null);

        Optional<Transaction> found = transactionRepository.findByIdAndUserId(saved.getId(), user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCategory()).isNull();
    }
}
