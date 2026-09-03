package io.github.rohergun.budgetmanager.summary;

import io.github.rohergun.budgetmanager.security.CustomUserDetails;
import io.github.rohergun.budgetmanager.security.CustomUserDetailsService;
import io.github.rohergun.budgetmanager.security.JwtService;
import io.github.rohergun.budgetmanager.security.LoginRateLimiter;
import io.github.rohergun.budgetmanager.summary.dto.CategorySpendingResponse;
import io.github.rohergun.budgetmanager.summary.dto.MonthlySummaryResponse;
import io.github.rohergun.budgetmanager.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SummaryController.class)
class SummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SummaryService summaryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private LoginRateLimiter loginRateLimiter;

    private UUID currentUserId;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();

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
    void getMonthlySummary_returnsOkWithSummary_whenMonthProvided() throws Exception {
        YearMonth requestedMonth = YearMonth.of(2026, 8);
        MonthlySummaryResponse response = new MonthlySummaryResponse(
                requestedMonth,
                new BigDecimal("1500.00"),
                new BigDecimal("240.00"),
                new BigDecimal("1260.00"),
                List.of(new CategorySpendingResponse(
                        UUID.randomUUID(), "Food", new BigDecimal("180.00"), new BigDecimal("200.00")))
        );

        when(summaryService.getMonthlyTransactionsSummary(currentUserId, requestedMonth))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/summaries/monthly")
                        .with(authentication(authentication))
                        .param("month", "2026-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(1500.00))
                .andExpect(jsonPath("$.totalExpenses").value(240.00))
                .andExpect(jsonPath("$.net").value(1260.00))
                .andExpect(jsonPath("$.byCategory[0].categoryName").value("Food"));

        verify(summaryService).getMonthlyTransactionsSummary(currentUserId, requestedMonth);
    }

    @Test
    void getMonthlySummary_defaultsToCurrentMonth_whenMonthOmitted() throws Exception {
        YearMonth currentMonth = YearMonth.now();
        MonthlySummaryResponse response = new MonthlySummaryResponse(
                currentMonth, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of());

        when(summaryService.getMonthlyTransactionsSummary(eq(currentUserId), eq(currentMonth)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/summaries/monthly")
                        .with(authentication(authentication)))
                .andExpect(status().isOk());

        verify(summaryService).getMonthlyTransactionsSummary(currentUserId, currentMonth);
    }

    @Test
    void getMonthlySummary_returnsBadRequest_whenMonthMalformed() throws Exception {
        mockMvc.perform(get("/api/v1/summaries/monthly")
                        .with(authentication(authentication))
                        .param("month", "not-a-month"))
                .andExpect(status().isBadRequest());

        verify(summaryService, never()).getMonthlyTransactionsSummary(any(), any());
    }
}