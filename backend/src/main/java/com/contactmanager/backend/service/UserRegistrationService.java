package com.contactmanager.backend.service;

import java.util.Locale;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.contactmanager.backend.dto.RegistrationRequest;
import com.contactmanager.backend.dto.RegistrationResponse;
import com.contactmanager.backend.entity.User;
import com.contactmanager.backend.entity.User.IdentifierType;
import com.contactmanager.backend.exception.RegistrationPersistenceException;
import com.contactmanager.backend.repository.UserRepository;

@Service
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final UserRegistrationWriter registrationWriter;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserRepository userRepository, UserRegistrationWriter registrationWriter,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.registrationWriter = registrationWriter;
        this.passwordEncoder = passwordEncoder;
    }

    public RegistrationResult register(RegistrationRequest request) {
        boolean usesEmail = request.email() != null && !request.email().isBlank();
        String identifier = usesEmail
                ? request.email().trim().toLowerCase(Locale.ROOT)
                : request.phone().trim();
        // Always perform the expensive password work so existing and new identifiers
        // follow comparable code paths and cannot be distinguished by a cheap timing probe.
        String passwordHash = passwordEncoder.encode(request.password());

        try {
            if (userRepository.existsByIdentifier(identifier)) {
                return registrationAccepted(false, identifier);
            }
        } catch (DataAccessException exception) {
            throw persistenceFailure(exception);
        }

        User user = new User(
                request.firstName().trim(),
                request.lastName().trim(),
                identifier,
                usesEmail ? IdentifierType.EMAIL : IdentifierType.PHONE,
                passwordHash);

        try {
            registrationWriter.insert(user);
            return registrationAccepted(true, identifier);
        } catch (DataIntegrityViolationException exception) {
            if (violatesIdentifierConstraint(exception)) {
                return registrationAccepted(false, identifier);
            }
            throw persistenceFailure(exception);
        } catch (DataAccessException exception) {
            throw persistenceFailure(exception);
        }
    }

    private boolean violatesIdentifierConstraint(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation
                    && constraintViolation.getConstraintName() != null
                    && constraintViolation.getConstraintName().toLowerCase(Locale.ROOT)
                            .contains("uk_users_identifier")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private RegistrationResult registrationAccepted(boolean created, String identifier) {
        RegistrationResponse response = new RegistrationResponse(
                null,
                null,
                null,
                null,
                null,
                "If the provided contact information is eligible, registration has been accepted");
        return new RegistrationResult(response, created, identifier);
    }

    private RegistrationPersistenceException persistenceFailure(DataAccessException cause) {
        return new RegistrationPersistenceException("Unable to persist registration", cause);
    }
}
