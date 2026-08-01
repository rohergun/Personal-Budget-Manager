package io.github.rohergun.budgetmanager.user;

import io.github.rohergun.budgetmanager.budget.Budget;
import io.github.rohergun.budgetmanager.model.BaseEntity;
import io.github.rohergun.budgetmanager.transaction.Transaction;
import io.github.rohergun.budgetmanager.financialgoal.FinancialGoal;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class AppUser extends BaseEntity {

    @Column(nullable = false, unique = true)
    @NotBlank
    private String email;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Column(nullable = false)
    @NotBlank
    private String surname;

    @Column(nullable = false, name = "password_hash")
    @NotBlank
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Budget> budgets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FinancialGoal> financialGoals = new ArrayList<>();
}
