package com.contactmanager.backend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class RegistrationRequestValidationTests {

    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void acceptsPasswordAtBcryptUtf8ByteBoundaryAndRejectsPasswordBeyondIt() {
        String eighteenEmojis = "😀".repeat(18);
        String nineteenEmojis = "😀".repeat(19);

        Set<ConstraintViolation<RegistrationRequest>> boundaryViolations = validator.validate(request(eighteenEmojis));
        Set<ConstraintViolation<RegistrationRequest>> oversizedViolations = validator.validate(request(nineteenEmojis));

        assertThat(boundaryViolations).isEmpty();
        assertThat(oversizedViolations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("passwordWithinBcryptLimit"));
    }

    private RegistrationRequest request(String password) {
        return new RegistrationRequest("Test", "User", "test@example.com", null, password);
    }
}
