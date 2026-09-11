package com.contactmanager.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;

import com.contactmanager.backend.dto.LoginRequest;
import com.contactmanager.backend.exception.ApiError;
import com.contactmanager.backend.exception.AuthenticationExceptionHandler;
import com.contactmanager.backend.service.UserAuthenticationService;
import com.contactmanager.backend.service.UserProfileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class AuthenticationControllerTests {

    @Test
    void loginReturnsServiceUnavailableWhenUserLookupFails() {
        UserAuthenticationService userAuthenticationService = mock(UserAuthenticationService.class);
        when(userAuthenticationService.loadUserByUsername("user@example.com"))
                .thenThrow(new TransientDataAccessResourceException("unavailable"));
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userAuthenticationService);
        provider.setPasswordEncoder(mock(PasswordEncoder.class));
        AuthenticationController controller = new AuthenticationController(
                new ProviderManager(provider),
                mock(SecurityContextRepository.class),
                mock(UserProfileService.class));

        InternalAuthenticationServiceException exception = assertThrows(
                InternalAuthenticationServiceException.class,
                () -> controller.login(
                        new LoginRequest("user@example.com", "valid-password"),
                        mock(HttpServletRequest.class),
                        mock(HttpServletResponse.class),
                        mock(CsrfToken.class)));

        ResponseEntity<ApiError> response = new AuthenticationExceptionHandler()
                .handleAuthenticationServiceFailure(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("Authentication is temporarily unavailable. Please try again later.");
    }
}
