package io.github.rohergun.budgetmanager.transaction;

import io.github.rohergun.budgetmanager.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "transactions")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class Transaction extends BaseEntity {

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;
}
