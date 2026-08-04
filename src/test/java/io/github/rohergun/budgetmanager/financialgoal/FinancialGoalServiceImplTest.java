package io.github.rohergun.budgetmanager.financialgoal;

import io.github.rohergun.budgetmanager.exception.BudgetManagerException;
import io.github.rohergun.budgetmanager.exception.DomainErrorMessage;
import io.github.rohergun.budgetmanager.financialgoal.dto.ContributeToGoalRequest;
import io.github.rohergun.budgetmanager.financialgoal.dto.CreateFinancialGoalRequest;
import io.github.rohergun.budgetmanager.financialgoal.dto.FinancialGoalResponse;
import io.github.rohergun.budgetmanager.financialgoal.dto.FinancialGoalUpdateRequest;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialGoalServiceImplTest {

    @Mock
    private FinancialGoalRepository goalRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private FinancialGoalMapper mapper;

    @InjectMocks
    private FinancialGoalServiceImpl goalService;

    private UUID currentUserId;
    private UUID goalId;
    private FinancialGoal existingGoal;

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

        existingGoal = FinancialGoal.builder()
                .name("Vacation")
                .description("Trip to Japan")
                .targetAmount(new BigDecimal("2000.00"))
                .currentAmount(new BigDecimal("500.00"))
                .deadline(LocalDateTime.now().plusMonths(6))
                .user(user)
                .build();
    }

    @Test
    void getGoalById_returnsMappedResponse_whenFoundAndOwned() {
        FinancialGoalResponse expectedResponse = new FinancialGoalResponse(
                goalId, "Vacation", "Trip to Japan",
                new BigDecimal("500.00"), new BigDecimal("2000.00"),
                LocalDateTime.now().plusMonths(6), LocalDateTime.now(), LocalDateTime.now());

        when(goalRepository.findByIdAndUserId(goalId, currentUserId))
                .thenReturn(Optional.of(existingGoal));
        when(mapper.toResponse(existingGoal)).thenReturn(expectedResponse);

        FinancialGoalResponse result = goalService.getGoalById(currentUserId, goalId);

        assertThat(result).isEqualTo(expectedResponse);
        verify(goalRepository).findByIdAndUserId(goalId, currentUserId);
    }

    @Test
    void getGoalById_throwsGoalNotFound_whenMissingOrNotOwned() {
        when(goalRepository.findByIdAndUserId(goalId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.getGoalById(currentUserId, goalId))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.FINANCIAL_GOAL_NOT_FOUND);

        verify(mapper, never()).toResponse(any());
    }

    @Test
    void listAllByCurrentUser_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 8);
        Page<FinancialGoal> goalPage = new PageImpl<>(List.of(existingGoal), pageable, 1);
        FinancialGoalResponse response = new FinancialGoalResponse(
                goalId, "Vacation", "Trip to Japan",
                new BigDecimal("500.00"), new BigDecimal("2000.00"),
                LocalDateTime.now().plusMonths(6), LocalDateTime.now(), LocalDateTime.now());

        when(goalRepository.findAllByUserId(currentUserId, pageable)).thenReturn(goalPage);
        when(mapper.toResponse(existingGoal)).thenReturn(response);

        Page<FinancialGoalResponse> result = goalService.listAllByCurrentUser(currentUserId, pageable);

        assertThat(result.getContent()).containsExactly(response);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(goalRepository).findAllByUserId(currentUserId, pageable);
    }

    @Test
    void addGoal_setsCurrentAmountToZero_savesAndReturnsMappedResponse() {
        CreateFinancialGoalRequest request = new CreateFinancialGoalRequest(
                "Vacation", "Trip to Japan", new BigDecimal("2000.00"),
                LocalDateTime.now().plusMonths(6));
        FinancialGoalResponse expectedResponse = new FinancialGoalResponse(
                goalId, "Vacation", "Trip to Japan",
                BigDecimal.ZERO, new BigDecimal("2000.00"),
                request.deadline(), LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.getReferenceById(currentUserId)).thenReturn(existingGoal.getUser());
        when(goalRepository.save(any(FinancialGoal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(FinancialGoal.class))).thenReturn(expectedResponse);

        FinancialGoalResponse result = goalService.addGoal(currentUserId, request);

        assertThat(result).isEqualTo(expectedResponse);

        var captor = org.mockito.ArgumentCaptor.forClass(FinancialGoal.class);
        verify(goalRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void updateGoal_updatesFieldsExceptCurrentAmount_andReturnsMappedResponse() {
        FinancialGoalUpdateRequest request = new FinancialGoalUpdateRequest(
                "Vacation Fund", "Trip to Italy", new BigDecimal("3000.00"),
                LocalDateTime.now().plusMonths(9));
        FinancialGoalResponse expectedResponse = new FinancialGoalResponse(
                goalId, "Vacation Fund", "Trip to Italy",
                new BigDecimal("500.00"), new BigDecimal("3000.00"),
                request.deadline(), LocalDateTime.now(), LocalDateTime.now());

        when(goalRepository.findByIdAndUserId(goalId, currentUserId))
                .thenReturn(Optional.of(existingGoal));
        when(mapper.toResponse(existingGoal)).thenReturn(expectedResponse);

        FinancialGoalResponse result = goalService.updateGoal(currentUserId, goalId, request);

        assertThat(existingGoal.getName()).isEqualTo("Vacation Fund");
        assertThat(existingGoal.getDescription()).isEqualTo("Trip to Italy");
        assertThat(existingGoal.getTargetAmount()).isEqualByComparingTo("3000.00");
        assertThat(existingGoal.getCurrentAmount()).isEqualByComparingTo("500.00"); // unchanged
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void updateGoal_throwsGoalNotFound_whenMissingOrNotOwned() {
        FinancialGoalUpdateRequest request = new FinancialGoalUpdateRequest(
                "Vacation Fund", "Trip to Italy", new BigDecimal("3000.00"),
                LocalDateTime.now().plusMonths(9));

        when(goalRepository.findByIdAndUserId(goalId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.updateGoal(currentUserId, goalId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.FINANCIAL_GOAL_NOT_FOUND);
    }

    @Test
    void contributeToGoal_addsAmountToCurrentAmount_andReturnsMappedResponse() {
        ContributeToGoalRequest request = new ContributeToGoalRequest(new BigDecimal("150.00"));
        FinancialGoalResponse expectedResponse = new FinancialGoalResponse(
                goalId, "Vacation", "Trip to Japan",
                new BigDecimal("650.00"), new BigDecimal("2000.00"),
                LocalDateTime.now().plusMonths(6), LocalDateTime.now(), LocalDateTime.now());

        when(goalRepository.findByIdAndUserId(goalId, currentUserId))
                .thenReturn(Optional.of(existingGoal));
        when(mapper.toResponse(existingGoal)).thenReturn(expectedResponse);

        FinancialGoalResponse result = goalService.contributeToGoal(currentUserId, goalId, request);

        assertThat(existingGoal.getCurrentAmount()).isEqualByComparingTo("650.00");
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void contributeToGoal_throwsGoalNotFound_whenMissingOrNotOwned() {
        ContributeToGoalRequest request = new ContributeToGoalRequest(new BigDecimal("150.00"));

        when(goalRepository.findByIdAndUserId(goalId, currentUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.contributeToGoal(currentUserId, goalId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.FINANCIAL_GOAL_NOT_FOUND);
    }

    @Test
    void deleteGoal_deletesById_whenFoundAndOwned() {
        when(goalRepository.existsByIdAndUserId(goalId, currentUserId)).thenReturn(true);

        goalService.deleteGoal(currentUserId, goalId);

        verify(goalRepository).deleteById(goalId);
    }

    @Test
    void deleteGoal_throwsGoalNotFound_whenMissingOrNotOwned() {
        when(goalRepository.existsByIdAndUserId(goalId, currentUserId)).thenReturn(false);

        assertThatThrownBy(() -> goalService.deleteGoal(currentUserId, goalId))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.FINANCIAL_GOAL_NOT_FOUND);

        verify(goalRepository, never()).deleteById(any());
    }
}
