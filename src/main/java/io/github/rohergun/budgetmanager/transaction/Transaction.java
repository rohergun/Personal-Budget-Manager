package io.github.rohergun.budgetmanager.transaction;

import io.github.rohergun.budgetmanager.category.Category;
import io.github.rohergun.budgetmanager.model.BaseEntity;
import io.github.rohergun.budgetmanager.user.AppUser;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
public class Transaction extends BaseEntity {

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Category category;

    @Column(nullable = false, name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
