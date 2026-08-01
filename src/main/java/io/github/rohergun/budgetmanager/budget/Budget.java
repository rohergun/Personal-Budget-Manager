package io.github.rohergun.budgetmanager.budget;

import io.github.rohergun.budgetmanager.category.Category;
import io.github.rohergun.budgetmanager.model.BaseEntity;
import io.github.rohergun.budgetmanager.user.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class Budget extends BaseEntity {

    @Column(name = "monthly_limit", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Category category;
}
