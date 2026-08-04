package io.github.rohergun.budgetmanager.financialgoal;

import io.github.rohergun.budgetmanager.exception.BudgetManagerException;
import io.github.rohergun.budgetmanager.exception.DomainErrorMessage;
import io.github.rohergun.budgetmanager.financialgoal.dto.ContributeToGoalRequest;
import io.github.rohergun.budgetmanager.financialgoal.dto.CreateFinancialGoalRequest;
import io.github.rohergun.budgetmanager.financialgoal.dto.FinancialGoalResponse;
import io.github.rohergun.budgetmanager.financialgoal.dto.FinancialGoalUpdateRequest;
import io.github.rohergun.budgetmanager.user.AppUser;
import io.github.rohergun.budgetmanager.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialGoalServiceImpl implements FinancialGoalService {

    private final FinancialGoalRepository goalRepository;
    private final AppUserRepository userRepository;
    private final FinancialGoalMapper mapper;

    @Override
    public FinancialGoalResponse getGoalById(UUID userId, UUID goalId) {
        FinancialGoal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.FINANCIAL_GOAL_NOT_FOUND));

        return mapper.toResponse(goal);
    }

    @Override
    public Page<FinancialGoalResponse> listAllByCurrentUser(UUID userId, Pageable pageable) {
        Page<FinancialGoal> goals = goalRepository.findAllByUserId(userId, pageable);
        return goals.map(mapper::toResponse);
    }

    @Override
    @Transactional
    public FinancialGoalResponse addGoal(UUID userId, CreateFinancialGoalRequest request) {
        AppUser user = userRepository.getReferenceById(userId);

        FinancialGoal newGoal = FinancialGoal.builder()
                .name(request.name())
                .description(request.description())
                .targetAmount(request.targetAmount())
                .currentAmount(BigDecimal.ZERO)
                .deadline(request.deadline())
                .user(user)
                .build();

        FinancialGoal saved = goalRepository.save(newGoal);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public FinancialGoalResponse updateGoal(UUID userId, UUID goalId, FinancialGoalUpdateRequest request) {
        FinancialGoal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.FINANCIAL_GOAL_NOT_FOUND));

        goal.setName(request.name());
        goal.setDescription(request.description());
        goal.setTargetAmount(request.targetAmount());
        goal.setDeadline(request.deadline());

        return mapper.toResponse(goal);
    }

    @Override
    @Transactional
    public FinancialGoalResponse contributeToGoal(UUID userId, UUID goalId, ContributeToGoalRequest request) {
        FinancialGoal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.FINANCIAL_GOAL_NOT_FOUND));

        goal.setCurrentAmount(goal.getCurrentAmount().add(request.amount()));

        return mapper.toResponse(goal);
    }

    @Override
    @Transactional
    public void deleteGoal(UUID userId, UUID goalId) {
        if (!goalRepository.existsByIdAndUserId(goalId, userId)) {
            throw new BudgetManagerException(DomainErrorMessage.FINANCIAL_GOAL_NOT_FOUND);
        }
        goalRepository.deleteById(goalId);
    }
}
