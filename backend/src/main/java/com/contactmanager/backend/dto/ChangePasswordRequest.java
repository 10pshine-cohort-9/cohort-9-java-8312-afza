package com.contactmanager.backend.dto;

import java.nio.charset.StandardCharsets;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters")
        String newPassword,

        @NotBlank(message = "Please confirm the new password")
        String confirmNewPassword) {

    @AssertTrue(message = "New password must not exceed 72 bytes when UTF-8 encoded")
    public boolean isNewPasswordWithinBcryptLimit() {
        return newPassword == null || newPassword.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @AssertTrue(message = "New passwords do not match")
    public boolean isNewPasswordsMatching() {
        return newPassword == null || newPassword.equals(confirmNewPassword);
    }
}
