package io.github.rohergun.budgetmanager.user.dto;

import java.util.UUID;

public record UserResponse (UUID id, String email, String name, String surname){ }
