package io.github.rohergun.budgetmanager.category;

import io.github.rohergun.budgetmanager.category.dto.CategoryResponse;
import io.github.rohergun.budgetmanager.category.dto.CategoryUpdateRequest;
import io.github.rohergun.budgetmanager.category.dto.CreateCategoryRequest;
import io.github.rohergun.budgetmanager.security.CustomUserDetails;
import io.github.rohergun.budgetmanager.security.CustomUserDetailsService;
import io.github.rohergun.budgetmanager.security.JwtService;
import io.github.rohergun.budgetmanager.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private UUID currentUserId;
    private UUID categoryId;
    private Authentication authentication;

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

        CustomUserDetails principal = new CustomUserDetails(user) {
            @Override
            public UUID getId() {
                return currentUserId;
            }
        };

        authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
    }

    @Test
    void getCategory_returnsOkWithCategoryResponse() throws Exception {
        CategoryResponse response = new CategoryResponse(
                categoryId, "Food", "Groceries and dining", LocalDateTime.now(), LocalDateTime.now());

        when(categoryService.getCategoryById(currentUserId, categoryId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/categories/{id}", categoryId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.description").value("Groceries and dining"));

        verify(categoryService).getCategoryById(currentUserId, categoryId);
    }

    @Test
    void listAll_returnsOkWithPageOfCategories() throws Exception {
        CategoryResponse response = new CategoryResponse(
                categoryId, "Food", "Groceries and dining", LocalDateTime.now(), LocalDateTime.now());
        Page<CategoryResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 8), 1);

        when(categoryService.listAllByCurrentUser(eq(currentUserId), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/categories")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Food"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(categoryService).listAllByCurrentUser(eq(currentUserId), any());
    }

    @Test
    void createCategory_returnsCreatedWithCategoryResponse() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest("Food", "Groceries and dining");
        CategoryResponse response = new CategoryResponse(
                categoryId, "Food", "Groceries and dining", LocalDateTime.now(), LocalDateTime.now());

        when(categoryService.addCategory(eq(currentUserId), any(CreateCategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/categories")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Food"));

        verify(categoryService).addCategory(currentUserId, request);
    }

    @Test
    void createCategory_returnsBadRequest_whenNameBlank() throws Exception {
        CreateCategoryRequest invalidRequest = new CreateCategoryRequest("", "Groceries and dining");

        mockMvc.perform(post("/api/v1/categories")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).addCategory(any(), any());
    }

    @Test
    void updateCategory_returnsOkWithUpdatedResponse() throws Exception {
        CategoryUpdateRequest request = new CategoryUpdateRequest("Transport", "Bus, train, taxi");
        CategoryResponse response = new CategoryResponse(
                categoryId, "Transport", "Bus, train, taxi", LocalDateTime.now(), LocalDateTime.now());

        when(categoryService.updateCategory(eq(currentUserId), eq(categoryId), any(CategoryUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/categories/{id}", categoryId)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Transport"))
                .andExpect(jsonPath("$.description").value("Bus, train, taxi"));

        verify(categoryService).updateCategory(currentUserId, categoryId, request);
    }

    @Test
    void deleteCategory_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(currentUserId, categoryId);
    }
}
