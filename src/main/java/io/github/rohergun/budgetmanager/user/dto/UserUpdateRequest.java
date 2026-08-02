package io.github.rohergun.budgetmanager.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank @Size(min = 2, max = 50, message = "Name length must be between {min} and {max} characters")
        String name,

        @NotBlank @Size(min = 2, max = 50, message = "Surname length must be between {min} and {max} characters")
        String surname) { }
