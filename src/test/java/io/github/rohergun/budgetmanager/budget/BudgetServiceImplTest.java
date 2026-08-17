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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private BudgetMapper mapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private UUID currentUserId;
    private UUID budgetId;
    private UUID categoryId;
    private AppUser user;
    private Category category;
    private Budget existingBudget;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        budgetId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        user = AppUser.builder()
                .email("john.doe@example.com")
                .name("John")
                .surname("Doe")
                .password("encoded-password")
                .build();

        category = Category.builder()
                .name("Food")
                .description("Groceries and dining")
                .user(user)
                .build();

        existingBudget = Budget.builder()
                .monthlyLimit(new BigDecimal("300.00"))
                .category(category)
                .user(user)
                .build();
    }

    @Test
    void getBudgetById_returnsMappedResponse_whenFoundAndOwned() {
        BudgetResponse expectedResponse = new BudgetResponse(
                budgetId, new BigDecimal("300.00"), categoryId, "Food", null, null);

        when(budgetRepository.findByIdAndUserId(budgetId, currentUserId))
                .thenReturn(Optional.of(existingBudget));
        when(mapper.toResponse(existingBudget)).thenReturn(expectedResponse);

        BudgetResponse result = budgetService.getBudgetById(currentUserId, budgetId);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getBudgetById_throwsBudgetNotFound_whenMissingOrNotOwned() {
        when(budgetRepository.findByIdAndUserId(budgetId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.getBudgetById(currentUserId, budgetId))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.BUDGET_NOT_FOUND);
    }

    @Test
    void addBudget_savesAndEvictsCurrentMonthCache_whenCategoryFreeAndOwned() {
        CreateBudgetRequest request = new CreateBudgetRequest(new BigDecimal("300.00"), categoryId);
        BudgetResponse expectedResponse = new BudgetResponse(
                budgetId, new BigDecimal("300.00"), categoryId, "Food", null, null);

        when(budgetRepository.existsByUserIdAndCategoryId(currentUserId, categoryId)).thenReturn(false);
        when(categoryRepository.findByIdAndUserId(categoryId, currentUserId))
                .thenReturn(Optional.of(category));
        when(userRepository.getReferenceById(currentUserId)).thenReturn(user);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cacheManager.getCache("monthlySummary")).thenReturn(cache);
        when(mapper.toResponse(any(Budget.class))).thenReturn(expectedResponse);

        BudgetResponse result = budgetService.addBudget(currentUserId, request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(cache).evict(currentUserId + "-" + YearMonth.now());

        ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);
        verify(budgetRepository).save(captor.capture());
        assertThat(captor.getValue().getCategory()).isEqualTo(category);
    }

    @Test
    void addBudget_throwsBudgetAlreadyExists_whenCategoryAlreadyBudgeted() {
        CreateBudgetRequest request = new CreateBudgetRequest(new BigDecimal("300.00"), categoryId);

        when(budgetRepository.existsByUserIdAndCategoryId(currentUserId, categoryId)).thenReturn(true);

        assertThatThrownBy(() -> budgetService.addBudget(currentUserId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.BUDGET_ALREADY_EXISTS);

        verify(budgetRepository, never()).save(any());
    }

    @Test
    void addBudget_throwsCategoryNotFound_whenCategoryMissingOrNotOwned() {
        CreateBudgetRequest request = new CreateBudgetRequest(new BigDecimal("300.00"), categoryId);

        when(budgetRepository.existsByUserIdAndCategoryId(currentUserId, categoryId)).thenReturn(false);
        when(categoryRepository.findByIdAndUserId(categoryId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.addBudget(currentUserId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.CATEGORY_NOT_FOUND);

        verify(budgetRepository, never()).save(any());
    }

    @Test
    void updateBudget_updatesAndEvictsCurrentMonthCache_whenFoundOwnedAndCategoryFree() {
        UUID newCategoryId = UUID.randomUUID();
        Category newCategory = Category.builder()
                .name("Transport")
                .description("Bus, train, taxi")
                .user(user)
                .build();

        BudgetUpdateRequest request = new BudgetUpdateRequest(new BigDecimal("450.00"), newCategoryId);
        BudgetResponse expectedResponse = new BudgetResponse(
                budgetId, new BigDecimal("450.00"), newCategoryId, "Transport", null, null);

        when(budgetRepository.findByIdAndUserId(budgetId, currentUserId))
                .thenReturn(Optional.of(existingBudget));
        when(budgetRepository.existsByUserIdAndCategoryIdAndIdNot(currentUserId, newCategoryId, budgetId))
                .thenReturn(false);
        when(categoryRepository.findByIdAndUserId(newCategoryId, currentUserId))
                .thenReturn(Optional.of(newCategory));
        when(cacheManager.getCache("monthlySummary")).thenReturn(cache);
        when(mapper.toResponse(existingBudget)).thenReturn(expectedResponse);

        BudgetResponse result = budgetService.updateBudget(currentUserId, budgetId, request);

        assertThat(existingBudget.getMonthlyLimit()).isEqualByComparingTo("450.00");
        assertThat(existingBudget.getCategory()).isEqualTo(newCategory);
        assertThat(result).isEqualTo(expectedResponse);
        verify(cache).evict(currentUserId + "-" + YearMonth.now());
    }

    @Test
    void updateBudget_throwsBudgetNotFound_whenMissingOrNotOwned() {
        BudgetUpdateRequest request = new BudgetUpdateRequest(new BigDecimal("450.00"), categoryId);

        when(budgetRepository.findByIdAndUserId(budgetId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.updateBudget(currentUserId, budgetId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.BUDGET_NOT_FOUND);

        verify(budgetRepository, never()).existsByUserIdAndCategoryIdAndIdNot(any(), any(), any());
    }

    @Test
    void updateBudget_throwsBudgetAlreadyExists_whenNewCategoryTakenByAnotherBudget() {
        UUID newCategoryId = UUID.randomUUID();
        BudgetUpdateRequest request = new BudgetUpdateRequest(new BigDecimal("450.00"), newCategoryId);

        when(budgetRepository.findByIdAndUserId(budgetId, currentUserId))
                .thenReturn(Optional.of(existingBudget));
        when(budgetRepository.existsByUserIdAndCategoryIdAndIdNot(currentUserId, newCategoryId, budgetId))
                .thenReturn(true);

        assertThatThrownBy(() -> budgetService.updateBudget(currentUserId, budgetId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.BUDGET_ALREADY_EXISTS);

        verify(categoryRepository, never()).findByIdAndUserId(any(), any());
    }

    @Test
    void updateBudget_throwsCategoryNotFound_whenNewCategoryMissingOrNotOwned() {
        UUID newCategoryId = UUID.randomUUID();
        BudgetUpdateRequest request = new BudgetUpdateRequest(new BigDecimal("450.00"), newCategoryId);

        when(budgetRepository.findByIdAndUserId(budgetId, currentUserId))
                .thenReturn(Optional.of(existingBudget));
        when(budgetRepository.existsByUserIdAndCategoryIdAndIdNot(currentUserId, newCategoryId, budgetId))
                .thenReturn(false);
        when(categoryRepository.findByIdAndUserId(newCategoryId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.updateBudget(currentUserId, budgetId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.CATEGORY_NOT_FOUND);
    }

    @Test
    void deleteBudget_deletesAndEvictsCurrentMonthCache_whenFoundAndOwned() {
        when(budgetRepository.existsByIdAndUserId(budgetId, currentUserId)).thenReturn(true);
        when(cacheManager.getCache("monthlySummary")).thenReturn(cache);

        budgetService.deleteBudget(currentUserId, budgetId);

        verify(budgetRepository).deleteById(budgetId);
        verify(cache).evict(currentUserId + "-" + YearMonth.now());
    }

    @Test
    void deleteBudget_throwsBudgetNotFound_whenMissingOrNotOwned() {
        when(budgetRepository.existsByIdAndUserId(budgetId, currentUserId)).thenReturn(false);

        assertThatThrownBy(() -> budgetService.deleteBudget(currentUserId, budgetId))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.BUDGET_NOT_FOUND);

        verify(budgetRepository, never()).deleteById(any());
    }
}