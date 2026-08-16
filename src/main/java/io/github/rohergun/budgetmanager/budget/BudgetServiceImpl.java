package io.github.rohergun.budgetmanager.budget;

import io.github.rohergun.budgetmanager.budget.dto.BudgetResponse;
import io.github.rohergun.budgetmanager.budget.dto.BudgetUpdateRequest;
import io.github.rohergun.budgetmanager.budget.dto.CreateBudgetRequest;
import io.github.rohergun.budgetmanager.category.Category;
import io.github.rohergun.budgetmanager.category.CategoryRepository;
import io.github.rohergun.budgetmanager.exception.BudgetManagerException;
import io.github.rohergun.budgetmanager.exception.DomainErrorMessage;
import io.github.rohergun.budgetmanager.user.AppUser;
import io.github.rohergun.budgetmanager.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final AppUserRepository userRepository;
    private final BudgetMapper mapper;

    @Autowired
    private CacheManager cacheManager;

    @Override
    public BudgetResponse getBudgetById(UUID userId, UUID budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.BUDGET_NOT_FOUND));

        return mapper.toResponse(budget);
    }

    @Override
    public Page<BudgetResponse> listAllByCurrentUser(UUID userId, Pageable pageable) {
        Page<Budget> budgets = budgetRepository.findAllByUserId(userId, pageable);
        return budgets.map(mapper::toResponse);
    }

    @Override
    public BudgetResponse getBudgetByCategory(UUID userId, UUID categoryId) {
        Budget budget = budgetRepository.findByUserIdAndCategoryId(userId, categoryId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.BUDGET_NOT_FOUND));

        return mapper.toResponse(budget);
    }

    @Override
    @Transactional
    public BudgetResponse addBudget(UUID userId, CreateBudgetRequest request) {
        if (budgetRepository.existsByUserIdAndCategoryId(userId, request.categoryId())) {
            throw new BudgetManagerException(DomainErrorMessage.BUDGET_ALREADY_EXISTS);
        }

        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.CATEGORY_NOT_FOUND));

        AppUser user = userRepository.getReferenceById(userId);

        Budget newBudget = Budget.builder()
                .monthlyLimit(request.monthlyLimit())
                .category(category)
                .user(user)
                .build();

        Budget saved = budgetRepository.save(newBudget);

        evictCurrentMonthSummary(userId);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(UUID userId, UUID budgetId, BudgetUpdateRequest request) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.BUDGET_NOT_FOUND));

        if (budgetRepository.existsByUserIdAndCategoryIdAndIdNot(userId, request.categoryId(), budgetId)) {
            throw new BudgetManagerException(DomainErrorMessage.BUDGET_ALREADY_EXISTS);
        }

        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.CATEGORY_NOT_FOUND));

        budget.setMonthlyLimit(request.monthlyLimit());
        budget.setCategory(category);

        evictCurrentMonthSummary(userId);
        return mapper.toResponse(budget);
    }

    @Override
    @Transactional
    public void deleteBudget(UUID userId, UUID budgetId) {
        if (!budgetRepository.existsByIdAndUserId(budgetId, userId)) {
            throw new BudgetManagerException(DomainErrorMessage.BUDGET_NOT_FOUND);
        }
        budgetRepository.deleteById(budgetId);
        evictCurrentMonthSummary(userId);
    }

    private void evictCurrentMonthSummary(UUID userId) {
        Cache cache = cacheManager.getCache("monthlySummary");
        if (cache != null) {
            cache.evict(userId + "-" + YearMonth.now());
        }
    }
}