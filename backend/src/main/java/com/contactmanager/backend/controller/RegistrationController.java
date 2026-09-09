package com.contactmanager.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.contactmanager.backend.dto.RegistrationRequest;
import com.contactmanager.backend.dto.RegistrationResponse;
import com.contactmanager.backend.service.UserRegistrationService;
import com.contactmanager.backend.service.AuthenticatedSessionService;
import com.contactmanager.backend.service.RegistrationResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Validated
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    private final UserRegistrationService registrationService;
    private final AuthenticatedSessionService authenticatedSessionService;

    public RegistrationController(UserRegistrationService registrationService,
            AuthenticatedSessionService authenticatedSessionService) {
        this.registrationService = registrationService;
        this.authenticatedSessionService = authenticatedSessionService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        RegistrationResult result = registrationService.register(request);
        if (result.created()) {
            try {
                authenticatedSessionService.authenticate(
                        result.identifier(), request.password(), httpRequest, httpResponse);
            } catch (RuntimeException exception) {
                logger.error("Registration committed, but automatic sign-in failed for the new account", exception);
                RegistrationResponse committedResponse = new RegistrationResponse(
                        null, null, null, null, null,
                        "Account created, but automatic sign-in failed. Please sign in.");
                return ResponseEntity.status(HttpStatus.CREATED).body(committedResponse);
            }
        }
        logger.info("Registration request accepted successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(result.response());
    }
}
