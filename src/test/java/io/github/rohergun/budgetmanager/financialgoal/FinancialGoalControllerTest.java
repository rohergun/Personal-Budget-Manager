package io.github.rohergun.budgetmanager.financialgoal;

import io.github.rohergun.budgetmanager.financialgoal.dto.ContributeToGoalRequest;
import io.github.rohergun.budgetmanager.financialgoal.dto.CreateFinancialGoalRequest;
import io.github.rohergun.budgetmanager.financialgoal.dto.FinancialGoalResponse;
import io.github.rohergun.budgetmanager.financialgoal.dto.FinancialGoalUpdateRequest;
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

@WebMvcTest(controllers = FinancialGoalController.class)
class FinancialGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private FinancialGoalService goalService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private UUID currentUserId;
    private UUID goalId;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        goalId = UUID.randomUUID();

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
    void getGoal_returnsOkWithGoalResponse() throws Exception {
        FinancialGoalResponse response = new FinancialGoalResponse(
                goalId, "Vacation", "Trip to Japan",
                new BigDecimal("2000.00"), new BigDecimal("500.00"),
                LocalDateTime.now().plusMonths(6), LocalDateTime.now(), LocalDateTime.now());

        when(goalService.getGoalById(currentUserId, goalId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/financial-goals/{id}", goalId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(goalId.toString()))
                .andExpect(jsonPath("$.name").value("Vacation"))
                .andExpect(jsonPath("$.targetAmount").value(2000.00))
                .andExpect(jsonPath("$.currentAmount").value(500.00));

        verify(goalService).getGoalById(currentUserId, goalId);
    }

    @Test
    void listAll_returnsOkWithPageOfGoals() throws Exception {
        FinancialGoalResponse response = new FinancialGoalResponse(
                goalId, "Vacation", "Trip to Japan",
                new BigDecimal("2000.00"), new BigDecimal("500.00"),
                LocalDateTime.now().plusMonths(6), LocalDateTime.now(), LocalDateTime.now());
        Page<FinancialGoalResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 8), 1);

        when(goalService.listAllByCurrentUser(eq(currentUserId), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/financial-goals")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Vacation"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(goalService).listAllByCurrentUser(eq(currentUserId), any());
    }

    @Test
    void createGoal_returnsCreatedWithGoalResponse() throws Exception {
        CreateFinancialGoalRequest request = new CreateFinancialGoalRequest(
                "Vacation", "Trip to Japan", new BigDecimal("2000.00"),
                LocalDateTime.now().plusMonths(6));
        FinancialGoalResponse response = new FinancialGoalResponse(
                goalId, "Vacation", "Trip to Japan",
                new BigDecimal("2000.00"), BigDecimal.ZERO,
                request.deadline(), LocalDateTime.now(), LocalDateTime.now());

        when(goalService.addGoal(eq(currentUserId), any(CreateFinancialGoalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/financial-goals")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Vacation"))
                .andExpect(jsonPath("$.currentAmount").value(0));

        verify(goalService).addGoal(currentUserId, request);
    }

    @Test
    void createGoal_returnsBadRequest_whenDeadlineInPast() throws Exception {
        CreateFinancialGoalRequest invalidRequest = new CreateFinancialGoalRequest(
                "Vacation", "Trip to Japan", new BigDecimal("2000.00"),
                LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/v1/financial-goals")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(goalService, never()).addGoal(any(), any());
    }

    @Test
    void updateGoal_returnsOkWithUpdatedResponse() throws Exception {
        FinancialGoalUpdateRequest request = new FinancialGoalUpdateRequest(
                "Vacation Fund", "Trip to Italy", new BigDecimal("3000.00"),
                LocalDateTime.now().plusMonths(9));
        FinancialGoalResponse response = new FinancialGoalResponse(
                goalId, "Vacation Fund", "Trip to Italy",
                new BigDecimal("3000.00"), new BigDecimal("500.00"),
                request.deadline(), LocalDateTime.now(), LocalDateTime.now());

        when(goalService.updateGoal(eq(currentUserId), eq(goalId), any(FinancialGoalUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/financial-goals/{id}", goalId)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vacation Fund"))
                .andExpect(jsonPath("$.targetAmount").value(3000.00));

        verify(goalService).updateGoal(currentUserId, goalId, request);
    }

    @Test
    void contributeToGoal_returnsOkWithUpdatedCurrentAmount() throws Exception {
        ContributeToGoalRequest request = new ContributeToGoalRequest(new BigDecimal("150.00"));
        FinancialGoalResponse response = new FinancialGoalResponse(
                goalId, "Vacation", "Trip to Japan",
                new BigDecimal("2000.00"), new BigDecimal("650.00"),
                LocalDateTime.now().plusMonths(6), LocalDateTime.now(), LocalDateTime.now());

        when(goalService.contributeToGoal(eq(currentUserId), eq(goalId), any(ContributeToGoalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/financial-goals/{id}/contributions", goalId)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentAmount").value(650.00));

        verify(goalService).contributeToGoal(currentUserId, goalId, request);
    }

    @Test
    void contributeToGoal_returnsBadRequest_whenAmountNotPositive() throws Exception {
        ContributeToGoalRequest invalidRequest = new ContributeToGoalRequest(BigDecimal.ZERO);

        mockMvc.perform(patch("/api/v1/financial-goals/{id}/contributions", goalId)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(goalService, never()).contributeToGoal(any(), any(), any());
    }

    @Test
    void deleteGoal_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/financial-goals/{id}", goalId)
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(goalService).deleteGoal(currentUserId, goalId);
    }
}