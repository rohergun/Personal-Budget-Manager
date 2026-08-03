package io.github.rohergun.budgetmanager.category;

import io.github.rohergun.budgetmanager.category.dto.CategoryResponse;
import io.github.rohergun.budgetmanager.category.dto.CategoryUpdateRequest;
import io.github.rohergun.budgetmanager.category.dto.CreateCategoryRequest;
import io.github.rohergun.budgetmanager.exception.BudgetManagerException;
import io.github.rohergun.budgetmanager.exception.DomainErrorMessage;
import io.github.rohergun.budgetmanager.user.AppUser;
import io.github.rohergun.budgetmanager.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private CategoryMapper mapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private UUID currentUserId;
    private UUID categoryId;
    private Category existingCategory;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        AppUser user = AppUser.builder()
                .email("john.doe@example.com")
                .name("John")
                .surname("Doe")
                .password("encoded-password")
                .build();

        existingCategory = Category.builder()
                .name("Food")
                .description("Groceries and dining")
                .user(user)
                .build();
    }

    @Test
    void getCategoryById_returnsMappedResponse_whenFoundAndOwned() {
        CategoryResponse expectedResponse = new CategoryResponse(
                categoryId, "Food", "Groceries and dining", LocalDateTime.now(), LocalDateTime.now());

        when(categoryRepository.findByIdAndUserId(categoryId, currentUserId))
                .thenReturn(Optional.of(existingCategory));
        when(mapper.toResponse(existingCategory)).thenReturn(expectedResponse);

        CategoryResponse result = categoryService.getCategoryById(currentUserId, categoryId);

        assertThat(result).isEqualTo(expectedResponse);
        verify(categoryRepository).findByIdAndUserId(categoryId, currentUserId);
    }

    @Test
    void getCategoryById_throwsCategoryNotFound_whenMissingOrNotOwned() {
        when(categoryRepository.findByIdAndUserId(categoryId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(currentUserId, categoryId))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.CATEGORY_NOT_FOUND);

        verify(mapper, never()).toResponse(any());
    }

    @Test
    void listAllByCurrentUser_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 8);
        Page<Category> categoryPage = new PageImpl<>(List.of(existingCategory), pageable, 1);
        CategoryResponse response = new CategoryResponse(
                categoryId, "Food", "Groceries and dining", LocalDateTime.now(), LocalDateTime.now());

        when(categoryRepository.findAllByUserId(currentUserId, pageable)).thenReturn(categoryPage);
        when(mapper.toResponse(existingCategory)).thenReturn(response);

        Page<CategoryResponse> result = categoryService.listAllByCurrentUser(currentUserId, pageable);

        assertThat(result.getContent()).containsExactly(response);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(categoryRepository).findAllByUserId(currentUserId, pageable);
    }

    @Test
    void addCategory_savesAndReturnsMappedResponse_whenNameNotTaken() {
        CreateCategoryRequest request = new CreateCategoryRequest("Food", "Groceries and dining");
        CategoryResponse expectedResponse = new CategoryResponse(
                categoryId, "Food", "Groceries and dining", LocalDateTime.now(), LocalDateTime.now());

        when(categoryRepository.existsByUserIdAndName(currentUserId, "Food")).thenReturn(false);
        when(userRepository.getReferenceById(currentUserId)).thenReturn(existingCategory.getUser());
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);
        when(mapper.toResponse(existingCategory)).thenReturn(expectedResponse);

        CategoryResponse result = categoryService.addCategory(currentUserId, request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void addCategory_throwsCategoryAlreadyExists_whenNameTaken() {
        CreateCategoryRequest request = new CreateCategoryRequest("Food", "Groceries and dining");

        when(categoryRepository.existsByUserIdAndName(currentUserId, "Food")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.addCategory(currentUserId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.CATEGORY_ALREADY_EXISTS);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_updatesAndReturnsMappedResponse_whenFoundOwnedAndNameFree() {
        CategoryUpdateRequest request = new CategoryUpdateRequest("Transport", "Bus, train, taxi");
        CategoryResponse expectedResponse = new CategoryResponse(
                categoryId, "Transport", "Bus, train, taxi", LocalDateTime.now(), LocalDateTime.now());

        when(categoryRepository.findByIdAndUserId(categoryId, currentUserId))
                .thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByUserIdAndNameAndIdNot(currentUserId, "Transport", categoryId))
                .thenReturn(false);
        when(mapper.toResponse(existingCategory)).thenReturn(expectedResponse);

        CategoryResponse result = categoryService.updateCategory(currentUserId, categoryId, request);

        assertThat(existingCategory.getName()).isEqualTo("Transport");
        assertThat(existingCategory.getDescription()).isEqualTo("Bus, train, taxi");
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void updateCategory_throwsCategoryNotFound_whenMissingOrNotOwned() {
        CategoryUpdateRequest request = new CategoryUpdateRequest("Transport", "Bus, train, taxi");

        when(categoryRepository.findByIdAndUserId(categoryId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(currentUserId, categoryId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.CATEGORY_NOT_FOUND);

        verify(categoryRepository, never()).existsByUserIdAndNameAndIdNot(any(), any(), any());
    }

    @Test
    void updateCategory_throwsCategoryAlreadyExists_whenNewNameTakenByAnotherCategory() {
        CategoryUpdateRequest request = new CategoryUpdateRequest("Transport", "Bus, train, taxi");

        when(categoryRepository.findByIdAndUserId(categoryId, currentUserId))
                .thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByUserIdAndNameAndIdNot(currentUserId, "Transport", categoryId))
                .thenReturn(true);

        assertThatThrownBy(() -> categoryService.updateCategory(currentUserId, categoryId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.CATEGORY_ALREADY_EXISTS);
    }

    @Test
    void deleteCategory_deletesById_whenFoundAndOwned() {
        when(categoryRepository.existsByIdAndUserId(categoryId, currentUserId)).thenReturn(true);

        categoryService.deleteCategory(currentUserId, categoryId);

        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void deleteCategory_throwsCategoryNotFound_whenMissingOrNotOwned() {
        when(categoryRepository.existsByIdAndUserId(categoryId, currentUserId)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.deleteCategory(currentUserId, categoryId))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.CATEGORY_NOT_FOUND);

        verify(categoryRepository, never()).deleteById(any());
    }
}