package io.github.rohergun.budgetmanager.transaction;

import io.github.rohergun.budgetmanager.security.CustomUserDetails;
import io.github.rohergun.budgetmanager.security.CustomUserDetailsService;
import io.github.rohergun.budgetmanager.security.JwtService;
import io.github.rohergun.budgetmanager.transaction.dto.CreateTransactionRequest;
import io.github.rohergun.budgetmanager.transaction.dto.TransactionResponse;
import io.github.rohergun.budgetmanager.transaction.dto.TransactionUpdateRequest;
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

@WebMvcTest(controllers = TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private UUID currentUserId;
    private UUID transactionId;
    private UUID categoryId;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
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
    void getTransaction_returnsOkWithTransactionResponse() throws Exception {
        TransactionResponse response = new TransactionResponse(
                transactionId, new BigDecimal("45.00"), TransactionType.EXPENSE,
                categoryId, "Food", LocalDateTime.now().minusDays(1),
                LocalDateTime.now(), LocalDateTime.now());

        when(transactionService.getTransactionById(currentUserId, transactionId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/{id}", transactionId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.amount").value(45.00))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
                .andExpect(jsonPath("$.categoryName").value("Food"));

        verify(transactionService).getTransactionById(currentUserId, transactionId);
    }

    @Test
    void listAll_returnsOkWithPageOfTransactions() throws Exception {
        TransactionResponse response = new TransactionResponse(
                transactionId, new BigDecimal("45.00"), TransactionType.EXPENSE,
                categoryId, "Food", LocalDateTime.now().minusDays(1),
                LocalDateTime.now(), LocalDateTime.now());
        Page<TransactionResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

        when(transactionService.listAllByCurrentUser(eq(currentUserId), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/transactions")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].categoryName").value("Food"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(transactionService).listAllByCurrentUser(eq(currentUserId), any());
    }

    @Test
    void createTransaction_returnsCreatedWithTransactionResponse() throws Exception {
        LocalDateTime transactionDate = LocalDateTime.now().minusDays(2);
        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("45.00"), TransactionType.EXPENSE, categoryId, transactionDate);
        TransactionResponse response = new TransactionResponse(
                transactionId, new BigDecimal("45.00"), TransactionType.EXPENSE,
                categoryId, "Food", transactionDate, LocalDateTime.now(), LocalDateTime.now());

        when(transactionService.addTransaction(eq(currentUserId), any(CreateTransactionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(45.00))
                .andExpect(jsonPath("$.type").value("EXPENSE"));

        verify(transactionService).addTransaction(currentUserId, request);
    }

    @Test
    void createTransaction_returnsBadRequest_whenAmountNotPositive() throws Exception {
        CreateTransactionRequest invalidRequest = new CreateTransactionRequest(
                BigDecimal.ZERO, TransactionType.EXPENSE, categoryId, LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/v1/transactions")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).addTransaction(any(), any());
    }

    @Test
    void createTransaction_returnsBadRequest_whenTransactionDateInFuture() throws Exception {
        CreateTransactionRequest invalidRequest = new CreateTransactionRequest(
                new BigDecimal("45.00"), TransactionType.EXPENSE, categoryId,
                LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/v1/transactions")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).addTransaction(any(), any());
    }

    @Test
    void updateTransaction_returnsOkWithUpdatedResponse() throws Exception {
        UUID newCategoryId = UUID.randomUUID();
        LocalDateTime newDate = LocalDateTime.now().minusDays(3);
        TransactionUpdateRequest request = new TransactionUpdateRequest(
                new BigDecimal("60.00"), TransactionType.INCOME, newCategoryId, newDate);
        TransactionResponse response = new TransactionResponse(
                transactionId, new BigDecimal("60.00"), TransactionType.INCOME,
                newCategoryId, "Transport", newDate, LocalDateTime.now(), LocalDateTime.now());

        when(transactionService.updateTransaction(eq(currentUserId), eq(transactionId), any(TransactionUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/transactions/{id}", transactionId)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(60.00))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.categoryName").value("Transport"));

        verify(transactionService).updateTransaction(currentUserId, transactionId, request);
    }

    @Test
    void deleteTransaction_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/transactions/{id}", transactionId)
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(transactionService).deleteTransaction(currentUserId, transactionId);
    }
}
