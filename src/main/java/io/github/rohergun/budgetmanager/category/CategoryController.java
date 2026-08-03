package io.github.rohergun.budgetmanager.category;

import io.github.rohergun.budgetmanager.category.dto.CategoryResponse;
import io.github.rohergun.budgetmanager.category.dto.CategoryUpdateRequest;
import io.github.rohergun.budgetmanager.category.dto.CreateCategoryRequest;
import io.github.rohergun.budgetmanager.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {

        return ResponseEntity.ok(categoryService.getCategoryById(principal.getId(), id));
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> listAll(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 8, sort = {"name"}, direction = Sort.Direction.ASC) Pageable pageable){

        return ResponseEntity.ok(categoryService.listAllByCurrentUser(principal.getId(), pageable));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid CreateCategoryRequest request) {

        CategoryResponse created = categoryService.addCategory(principal.getId(), request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @RequestBody @Valid CategoryUpdateRequest request) {

        CategoryResponse updated = categoryService.updateCategory(principal.getId(), id, request);
        return ResponseEntity.status(200).body(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {

        categoryService.deleteCategory(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
