package io.github.rohergun.budgetmanager.user.dto;

import java.util.UUID;

public record UserResponse (UUID user_id, String email, String name, String surname){ }
