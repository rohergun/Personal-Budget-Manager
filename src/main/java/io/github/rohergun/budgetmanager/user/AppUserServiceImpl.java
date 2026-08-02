package io.github.rohergun.budgetmanager.user;

import io.github.rohergun.budgetmanager.exception.BudgetManagerException;
import io.github.rohergun.budgetmanager.exception.DomainErrorMessage;
import io.github.rohergun.budgetmanager.user.dto.PasswordUpdateRequest;
import io.github.rohergun.budgetmanager.user.dto.UserResponse;
import io.github.rohergun.budgetmanager.user.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService{

    private final AppUserRepository userRepository;
    private final AppUserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getCurrentUserProfile(UUID userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.USER_NOT_FOUND));

        return mapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUserProfile(UUID userId, UserUpdateRequest request) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.USER_NOT_FOUND));
        user.setName(request.name());
        user.setSurname(request.surname());

        userRepository.save(user);

        return mapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUserPassword(UUID userId, PasswordUpdateRequest request) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BudgetManagerException(DomainErrorMessage.INVALID_CREDENTIALS);
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        return mapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deleteCurrentUser(UUID userId) {
        userRepository.deleteById(userId);
    }
}
