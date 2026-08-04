package io.github.rohergun.budgetmanager.budget;

import io.github.rohergun.budgetmanager.budget.dto.BudgetResponse;
import io.github.rohergun.budgetmanager.budget.dto.BudgetUpdateRequest;
import io.github.rohergun.budgetmanager.budget.dto.CreateBudgetRequest;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BudgetController.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private BudgetService budgetService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private UUID currentUserId;
    private UUID budgetId;
    private UUID categoryId;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        budgetId = UUID.randomUUID();
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
    void getBudget_returnsOkWithBudgetResponse() throws Exception {
        BudgetResponse response = new BudgetResponse(
                budgetId, new BigDecimal("300.00"), categoryId, "Food", null, null);

        when(budgetService.getBudgetById(currentUserId, budgetId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/budgets/{id}", budgetId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budgetId.toString()))
                .andExpect(jsonPath("$.monthlyLimit").value(300.00))
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
                .andExpect(jsonPath("$.categoryName").value("Food"));

        verify(budgetService).getBudgetById(currentUserId, budgetId);
    }

    @Test
    void listAll_returnsOkWithPageOfBudgets() throws Exception {
        BudgetResponse response = new BudgetResponse(
                budgetId, new BigDecimal("300.00"), categoryId, "Food", null, null);
        Page<BudgetResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 8), 1);

        when(budgetService.listAllByCurrentUser(eq(currentUserId), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/budgets")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].categoryName").value("Food"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(budgetService).listAllByCurrentUser(eq(currentUserId), any());
    }

    @Test
    void getBudgetByCategory_returnsOkWithBudgetResponse() throws Exception {
        BudgetResponse response = new BudgetResponse(
                budgetId, new BigDecimal("300.00"), categoryId, "Food", null, null);

        when(budgetService.getBudgetByCategory(currentUserId, categoryId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/budgets/by-category/{categoryId}", categoryId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()));

        verify(budgetService).getBudgetByCategory(currentUserId, categoryId);
    }

    @Test
    void createBudget_returnsCreatedWithBudgetResponse() throws Exception {
        CreateBudgetRequest request = new CreateBudgetRequest(new BigDecimal("300.00"), categoryId);
        BudgetResponse response = new BudgetResponse(
                budgetId, new BigDecimal("300.00"), categoryId, "Food", null, null);

        when(budgetService.addBudget(eq(currentUserId), any(CreateBudgetRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/budgets")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.monthlyLimit").value(300.00))
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()));

        verify(budgetService).addBudget(currentUserId, request);
    }

    @Test
    void createBudget_returnsBadRequest_whenMonthlyLimitNotPositive() throws Exception {
        CreateBudgetRequest invalidRequest = new CreateBudgetRequest(BigDecimal.ZERO, categoryId);

        mockMvc.perform(post("/api/v1/budgets")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(budgetService, never()).addBudget(any(), any());
    }

    @Test
    void createBudget_returnsBadRequest_whenCategoryIdMissing() throws Exception {
        CreateBudgetRequest invalidRequest = new CreateBudgetRequest(new BigDecimal("300.00"), null);

        mockMvc.perform(post("/api/v1/budgets")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(budgetService, never()).addBudget(any(), any());
    }

    @Test
    void updateBudget_returnsOkWithUpdatedResponse() throws Exception {
        UUID newCategoryId = UUID.randomUUID();
        BudgetUpdateRequest request = new BudgetUpdateRequest(new BigDecimal("450.00"), newCategoryId);
        BudgetResponse response = new BudgetResponse(
                budgetId, new BigDecimal("450.00"), newCategoryId, "Transport", null, null);

        when(budgetService.updateBudget(eq(currentUserId), eq(budgetId), any(BudgetUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/budgets/{id}", budgetId)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyLimit").value(450.00))
                .andExpect(jsonPath("$.categoryName").value("Transport"));

        verify(budgetService).updateBudget(currentUserId, budgetId, request);
    }

    @Test
    void deleteBudget_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/budgets/{id}", budgetId)
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(budgetService).deleteBudget(currentUserId, budgetId);
    }
}