package io.github.rohergun.budgetmanager.category;

import io.github.rohergun.budgetmanager.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@AllArgsConstructor @NoArgsConstructor
@Setter @Getter
public class Category extends BaseEntity {

    @Column(name = "category_name", nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;
}
