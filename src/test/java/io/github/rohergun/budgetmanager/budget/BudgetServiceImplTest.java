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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
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
        verify(budgetRepository).findByIdAndUserId(budgetId, currentUserId);
    }

    @Test
    void getBudgetById_throwsBudgetNotFound_whenMissingOrNotOwned() {
        when(budgetRepository.findByIdAndUserId(budgetId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.getBudgetById(currentUserId, budgetId))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.BUDGET_NOT_FOUND);

        verify(mapper, never()).toResponse(any());
    }

    @Test
    void listAllByCurrentUser_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 8);
        Page<Budget> budgetPage = new PageImpl<>(List.of(existingBudget), pageable, 1);
        BudgetResponse response = new BudgetResponse(
                budgetId, new BigDecimal("300.00"), categoryId, "Food", null, null);

        when(budgetRepository.findAllByUserId(currentUserId, pageable)).thenReturn(budgetPage);
        when(mapper.toResponse(existingBudget)).thenReturn(response);

        Page<BudgetResponse> result = budgetService.listAllByCurrentUser(currentUserId, pageable);

        assertThat(result.getContent()).containsExactly(response);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(budgetRepository).findAllByUserId(currentUserId, pageable);
    }

    @Test
    void getBudgetByCategory_returnsMappedResponse_whenFound() {
        BudgetResponse expectedResponse = new BudgetResponse(
                budgetId, new BigDecimal("300.00"), categoryId, "Food", null, null);

        when(budgetRepository.findByUserIdAndCategoryId(currentUserId, categoryId))
                .thenReturn(Optional.of(existingBudget));
        when(mapper.toResponse(existingBudget)).thenReturn(expectedResponse);

        BudgetResponse result = budgetService.getBudgetByCategory(currentUserId, categoryId);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getBudgetByCategory_throwsBudgetNotFound_whenMissing() {
        when(budgetRepository.findByUserIdAndCategoryId(currentUserId, categoryId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.getBudgetByCategory(currentUserId, categoryId))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.BUDGET_NOT_FOUND);
    }

    @Test
    void addBudget_savesAndReturnsMappedResponse_whenCategoryFreeAndOwned() {
        CreateBudgetRequest request = new CreateBudgetRequest(new BigDecimal("300.00"), categoryId);
        BudgetResponse expectedResponse = new BudgetResponse(
                budgetId, new BigDecimal("300.00"), categoryId, "Food", null, null);

        when(budgetRepository.existsByUserIdAndCategoryId(currentUserId, categoryId)).thenReturn(false);
        when(categoryRepository.findByIdAndUserId(categoryId, currentUserId))
                .thenReturn(Optional.of(category));
        when(userRepository.getReferenceById(currentUserId)).thenReturn(user);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Budget.class))).thenReturn(expectedResponse);

        BudgetResponse result = budgetService.addBudget(currentUserId, request);

        assertThat(result).isEqualTo(expectedResponse);

        ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);
        verify(budgetRepository).save(captor.capture());
        assertThat(captor.getValue().getCategory()).isEqualTo(category);
        assertThat(captor.getValue().getMonthlyLimit()).isEqualByComparingTo("300.00");
    }

    @Test
    void addBudget_throwsBudgetAlreadyExists_whenCategoryAlreadyBudgeted() {
        CreateBudgetRequest request = new CreateBudgetRequest(new BigDecimal("300.00"), categoryId);

        when(budgetRepository.existsByUserIdAndCategoryId(currentUserId, categoryId)).thenReturn(true);

        assertThatThrownBy(() -> budgetService.addBudget(currentUserId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.BUDGET_ALREADY_EXISTS);

        verify(categoryRepository, never()).findByIdAndUserId(any(), any());
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
    void updateBudget_updatesLimitAndCategory_whenFoundOwnedAndCategoryFree() {
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
        when(mapper.toResponse(existingBudget)).thenReturn(expectedResponse);

        BudgetResponse result = budgetService.updateBudget(currentUserId, budgetId, request);

        assertThat(existingBudget.getMonthlyLimit()).isEqualByComparingTo("450.00");
        assertThat(existingBudget.getCategory()).isEqualTo(newCategory);
        assertThat(result).isEqualTo(expectedResponse);
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
    void deleteBudget_deletesById_whenFoundAndOwned() {
        when(budgetRepository.existsByIdAndUserId(budgetId, currentUserId)).thenReturn(true);

        budgetService.deleteBudget(currentUserId, budgetId);

        verify(budgetRepository).deleteById(budgetId);
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