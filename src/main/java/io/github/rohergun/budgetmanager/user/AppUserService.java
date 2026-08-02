package io.github.rohergun.budgetmanager.user;

import io.github.rohergun.budgetmanager.user.dto.PasswordUpdateRequest;
import io.github.rohergun.budgetmanager.user.dto.UserResponse;
import io.github.rohergun.budgetmanager.user.dto.UserUpdateRequest;

import java.util.UUID;

public interface AppUserService {
    UserResponse getCurrentUserProfile(UUID userId);
    UserResponse updateCurrentUserProfile(UUID userId, UserUpdateRequest request);
    UserResponse updateCurrentUserPassword(UUID userId, PasswordUpdateRequest request);
    void deleteCurrentUser(UUID userId);
}
