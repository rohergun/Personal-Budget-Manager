package io.github.rohergun.budgetmanager.user;

import io.github.rohergun.budgetmanager.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


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

    // Budgets, transactions, financial goals and categories are removed by the
    // database (ON DELETE CASCADE on their user_id foreign keys) when a user is deleted.
}
