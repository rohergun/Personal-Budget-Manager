package io.github.rohergun.budgetmanager.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest (
        @NotBlank @Size(min = 2, max = 20)
        String name,
        @Size(min = 3, max = 100)
        String description
){ }
