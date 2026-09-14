package com.contactmanager.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.contactmanager.backend.dto.RegistrationRequest;
import com.contactmanager.backend.dto.RegistrationResponse;
import com.contactmanager.backend.service.UserRegistrationService;
import com.contactmanager.backend.service.AuthenticatedSessionService;
import com.contactmanager.backend.service.RegistrationResult;

class RegistrationControllerTests {

    private UserRegistrationService registrationService;
    private AuthenticatedSessionService authenticatedSessionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        registrationService = mock(UserRegistrationService.class);
        authenticatedSessionService = mock(AuthenticatedSessionService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new RegistrationController(registrationService, authenticatedSessionService))
                .setControllerAdvice(new com.contactmanager.backend.exception.RegistrationExceptionHandler())
                .build();
    }

    @Test
    void validRegistrationReturnsCreatedContract() throws Exception {
        RegistrationResponse response = new RegistrationResponse(
                null, null, null, null, null, "Registration accepted");
        when(registrationService.register(any(RegistrationRequest.class)))
                .thenReturn(new RegistrationResult(response, true, "ada@example.com"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration accepted"));

        verify(registrationService).register(any(RegistrationRequest.class));
        verify(authenticatedSessionService).authenticate(
                eq("ada@example.com"), eq("password123"), any(), any());
    }

    @Test
    void acceptedDuplicateDoesNotAuthenticateExistingAccount() throws Exception {
        RegistrationResponse response = new RegistrationResponse(
                null, null, null, null, null, "Registration accepted");
        when(registrationService.register(any(RegistrationRequest.class)))
                .thenReturn(new RegistrationResult(response, false, "ada@example.com"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration accepted"));

        verify(authenticatedSessionService, never()).authenticate(any(), any(), any(), any());
    }

    @Test
    void committedRegistrationReportsRecoverableAutomaticSignInFailure() throws Exception {
        RegistrationResponse response = new RegistrationResponse(
                null, null, null, null, null, "Registration accepted");
        when(registrationService.register(any(RegistrationRequest.class)))
                .thenReturn(new RegistrationResult(response, true, "ada@example.com"));
        when(authenticatedSessionService.authenticate(any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("session unavailable"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(
                        "Account created, but automatic sign-in failed. Please sign in."));
    }

    @Test
    void invalidRegistrationReturnsFieldErrorsWithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"","lastName":"","email":"not-an-email","phone":"123","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Registration details are invalid"))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.lastName").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.identifier").exists());

        verifyNoInteractions(registrationService);
        verifyNoInteractions(authenticatedSessionService);
    }
}
