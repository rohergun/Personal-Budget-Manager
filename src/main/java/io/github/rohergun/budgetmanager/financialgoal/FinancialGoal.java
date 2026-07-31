package io.github.rohergun.budgetmanager.financialgoal;

import io.github.rohergun.budgetmanager.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_goals")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class FinancialGoal extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, name = "target_amount")
    private BigDecimal targetAmount;

    @Column(name = "current_amount")
    private BigDecimal currentAmount;

    @Column(nullable = false)
    private LocalDateTime deadline;
}
