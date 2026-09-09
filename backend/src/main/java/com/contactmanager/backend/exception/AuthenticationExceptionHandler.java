package com.contactmanager.backend.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.contactmanager.backend.controller.AuthenticationController;
import com.contactmanager.backend.controller.UserProfileController;

@RestControllerAdvice(assignableTypes = { AuthenticationController.class, UserProfileController.class })
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthenticationExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationExceptionHandler.class);

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials() {
        logger.warn("Login failed due to invalid credentials");
        return error(HttpStatus.UNAUTHORIZED, "Email/phone number or password is incorrect", Map.of());
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    public ResponseEntity<ApiError> handleInvalidCurrentPassword(InvalidCurrentPasswordException exception) {
        logger.warn("Password change failed because the current password was invalid");
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(),
                Map.of("currentPassword", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        logger.warn("Authentication or profile request validation failed");
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String field = switch (fieldError.getField()) {
                case "newPasswordWithinBcryptLimit" -> "newPassword";
                case "newPasswordsMatching" -> "confirmNewPassword";
                default -> fieldError.getField();
            };
            errors.putIfAbsent(field, fieldError.getDefaultMessage());
        });
        return error(HttpStatus.BAD_REQUEST, "Please correct the highlighted fields", errors);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleMissingAuthenticatedUser() {
        logger.warn("Authenticated request failed because the user principal was unavailable");
        return error(HttpStatus.UNAUTHORIZED, "Authentication is no longer valid", Map.of());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, Map<String, String> errors) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), message, errors));
    }
}
