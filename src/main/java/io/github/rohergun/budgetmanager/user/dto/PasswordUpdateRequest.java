package io.github.rohergun.budgetmanager.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateRequest(
        @NotBlank @Valid String currentPassword,
        @NotBlank @Size(min = 8, max = 50)
        String password
) { }
