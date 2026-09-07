package com.contactmanager.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email or phone number is required")
        @Size(max = 254, message = "Email or phone number is too long")
        String identifier,

        @NotBlank(message = "Password is required")
        String password) {
}
