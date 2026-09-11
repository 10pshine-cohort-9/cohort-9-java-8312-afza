package com.contactmanager.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

class RegistrationControllerTests {

    private UserRegistrationService registrationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        registrationService = mock(UserRegistrationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new RegistrationController(registrationService))
                .setControllerAdvice(new com.contactmanager.backend.exception.RegistrationExceptionHandler())
                .build();
    }

    @Test
    void validRegistrationReturnsCreatedContract() throws Exception {
        RegistrationResponse response = new RegistrationResponse(
                null, null, null, null, null, "Registration accepted");
        when(registrationService.register(any(RegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration accepted"));

        verify(registrationService).register(any(RegistrationRequest.class));
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
    }
}
