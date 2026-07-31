package io.github.rohergun.budgetmanager.budget;

import io.github.rohergun.budgetmanager.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class Budget extends BaseEntity {

    @Column(name = "monthly_limit", nullable = false)
    private BigDecimal monthlyLimit;
}
