package io.github.rohergun.budgetmanager.category;

import io.github.rohergun.budgetmanager.category.dto.CategoryResponse;
import io.github.rohergun.budgetmanager.category.dto.CategoryUpdateRequest;
import io.github.rohergun.budgetmanager.category.dto.CreateCategoryRequest;
import io.github.rohergun.budgetmanager.exception.BudgetManagerException;
import io.github.rohergun.budgetmanager.exception.DomainErrorMessage;
import io.github.rohergun.budgetmanager.user.AppUser;
import io.github.rohergun.budgetmanager.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;
    private final AppUserRepository userRepository;
    private final CategoryMapper mapper;

    @Override
    public CategoryResponse getCategoryById(UUID userId, UUID categoryId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.CATEGORY_NOT_FOUND));

        return mapper.toResponse(category);
    }

    @Override
    public Page<CategoryResponse> listAllByCurrentUser(UUID userId, Pageable pageable) {
        Page<Category> categories = categoryRepository.findAllByUserId(userId, pageable);
        return categories.map(mapper::toResponse);
    }

    @Override
    @Transactional
    public CategoryResponse addCategory(UUID userId, CreateCategoryRequest request) {
        if (!categoryRepository.existsByUserIdAndName(userId, request.name())) {
            throw new BudgetManagerException(DomainErrorMessage.CATEGORY_ALREADY_EXISTS);
        }

        AppUser user = userRepository.getReferenceById(userId);

        Category newCategory = Category.builder().
                name(request.name())
                .description(request.description())
                .user(user)
                .build();

        Category saved = categoryRepository.save(newCategory);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID userId, UUID categoryId, CategoryUpdateRequest request) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.CATEGORY_NOT_FOUND));

        if (categoryRepository.existsByUserIdAndNameAndIdNot(userId, request.name(), categoryId)) {
            throw new BudgetManagerException(DomainErrorMessage.CATEGORY_ALREADY_EXISTS);
        }

        category.setName(request.name());
        category.setDescription(request.description());

        return mapper.toResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID userId, UUID categoryId) {
        if (!categoryRepository.existsByIdAndUserId(categoryId, userId)) {
            throw new BudgetManagerException(DomainErrorMessage.CATEGORY_NOT_FOUND);
        }
        categoryRepository.deleteById(categoryId);
    }
}
