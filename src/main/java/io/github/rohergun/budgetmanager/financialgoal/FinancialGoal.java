package io.github.rohergun.budgetmanager.financialgoal;

import io.github.rohergun.budgetmanager.model.BaseEntity;
import io.github.rohergun.budgetmanager.user.AppUser;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_goals")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_financial_goals_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AppUser user;
}
