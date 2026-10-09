package io.github.rohergun.budgetmanager.category;

import io.github.rohergun.budgetmanager.model.BaseEntity;
import io.github.rohergun.budgetmanager.user.AppUser;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_name"}))
@AllArgsConstructor @NoArgsConstructor
@Setter @Getter
@Builder
public class Category extends BaseEntity {

    @Column(name = "category_name", nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_categories_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AppUser user;
}
