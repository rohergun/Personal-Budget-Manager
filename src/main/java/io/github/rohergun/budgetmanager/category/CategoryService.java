package io.github.rohergun.budgetmanager.category;

import io.github.rohergun.budgetmanager.category.dto.CategoryResponse;
import io.github.rohergun.budgetmanager.category.dto.CategoryUpdateRequest;
import io.github.rohergun.budgetmanager.category.dto.CreateCategoryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse getCategoryById(UUID userId, UUID categoryId);
    Page<CategoryResponse> listAllByCurrentUser(UUID userId, Pageable pageable);
    CategoryResponse addCategory(UUID userId, CreateCategoryRequest request);
    CategoryResponse updateCategory(UUID userId, UUID categoryId, CategoryUpdateRequest request);
    void deleteCategory(UUID userId, UUID categoryId);
}
