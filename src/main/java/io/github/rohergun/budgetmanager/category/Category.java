package io.github.rohergun.budgetmanager.category;

import io.github.rohergun.budgetmanager.model.BaseEntity;
import io.github.rohergun.budgetmanager.user.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_name"}))
@AllArgsConstructor @NoArgsConstructor
@Setter @Getter
public class Category extends BaseEntity {

    @Column(name = "category_name", nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
}
