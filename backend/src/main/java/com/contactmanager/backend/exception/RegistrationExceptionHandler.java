package com.contactmanager.backend.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.contactmanager.backend.controller.RegistrationController;

@RestControllerAdvice(assignableTypes = RegistrationController.class)
public class RegistrationExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationExceptionHandler.class);

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiError> handleDuplicateUser(DuplicateUserException exception) {
        logger.warn("Registration failed because the user already exists");
        return error(HttpStatus.CONFLICT, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        logger.warn("Registration request validation failed");
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> {
                    String field = switch (error.getField()) {
                        case "exactlyOneIdentifierProvided" -> "identifier";
                        case "passwordWithinBcryptLimit" -> "password";
                        default -> error.getField();
                    };
                    errors.putIfAbsent(field, error.getDefaultMessage());
                });
        exception.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.putIfAbsent("identifier", error.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Registration details are invalid", errors);
    }

    @ExceptionHandler(RegistrationPersistenceException.class)
    public ResponseEntity<ApiError> handlePersistenceFailure(RegistrationPersistenceException exception) {
        logger.error("Registration failed because the data store was unavailable", exception);
        return error(HttpStatus.SERVICE_UNAVAILABLE,
                "Registration is temporarily unavailable. Please try again later.", Map.of());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, Map<String, String> errors) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), message, errors));
    }
}
