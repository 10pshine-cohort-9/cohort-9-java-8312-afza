package com.contactmanager.backend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class ChangePasswordRequestValidationTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void enforcesRegistrationPasswordLengthAndConfirmation() {
        ChangePasswordRequest request = new ChangePasswordRequest("current", "short", "different");

        Set<String> messages = validator.validate(request).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertThat(messages)
                .contains("New password must be at least 8 characters", "New passwords do not match");
    }

    @Test
    void rejectsPasswordBeyondBcryptByteLimit() {
        String password = "é".repeat(37);
        ChangePasswordRequest request = new ChangePasswordRequest("current", password, password);

        assertThat(validator.validate(request))
                .extracting(ConstraintViolation::getMessage)
                .contains("New password must not exceed 72 bytes when UTF-8 encoded");
    }
}
