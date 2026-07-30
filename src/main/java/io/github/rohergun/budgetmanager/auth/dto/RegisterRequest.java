package io.github.rohergun.budgetmanager.auth.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest (
        @Email @NotBlank
        String email,

        @NotBlank @Size(min = 2, max = 50, message = "Name length must be between {min} and {max} characters")
        String name,

        @NotBlank @Size(min = 2, max = 50, message = "Surname length must be between {min} and {max} characters")
        String surname,

        @NotBlank @Size(min = 8, max = 50)
        String password
){ }
