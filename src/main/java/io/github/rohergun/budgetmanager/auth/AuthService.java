package io.github.rohergun.budgetmanager.auth;

import io.github.rohergun.budgetmanager.auth.dto.AuthResponse;
import io.github.rohergun.budgetmanager.auth.dto.LoginRequest;
import io.github.rohergun.budgetmanager.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
