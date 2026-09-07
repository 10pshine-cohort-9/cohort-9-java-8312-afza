package com.contactmanager.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;

import com.contactmanager.backend.dto.LoginRequest;
import com.contactmanager.backend.dto.UserProfileResponse;
import com.contactmanager.backend.service.AuthenticatedUser;
import com.contactmanager.backend.service.AuthenticatedSessionService;
import com.contactmanager.backend.service.UserProfileService;

class AuthenticationControllerTests {

    private AuthenticatedSessionService authenticatedSessionService;
    private UserProfileService profileService;
    private AuthenticationController controller;

    @BeforeEach
    void setUp() {
        authenticatedSessionService = mock(AuthenticatedSessionService.class);
        profileService = mock(UserProfileService.class);
        controller = new AuthenticationController(authenticatedSessionService, profileService);
    }

    @Test
    void successfulLoginNormalizesIdentifierSavesSessionContextAndReturnsProfile() {
        UserProfileResponse profile = new UserProfileResponse(7L, "Test", "User", "user@example.com", null);
        CsrfToken csrfToken = mock(CsrfToken.class);
        when(csrfToken.getToken()).thenReturn("token");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authenticatedSessionService.authenticate(
                " User@Example.com ", "password", request, response)).thenReturn(profile);

        UserProfileResponse result = controller.login(
                new LoginRequest(" User@Example.com ", "password"), request, response, csrfToken);

        assertThat(result).isEqualTo(profile);
        verify(authenticatedSessionService).authenticate(
                " User@Example.com ", "password", request, response);
    }

    @Test
    void invalidLoginPropagatesAuthenticationFailureForApiHandler() {
        when(authenticatedSessionService.authenticate(any(), any(), any(), any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> controller.login(
                new LoginRequest("user@example.com", "wrong-password"),
                new MockHttpServletRequest(), new MockHttpServletResponse(), mock(CsrfToken.class)))
                .isInstanceOf(BadCredentialsException.class);

        assertThat(new com.contactmanager.backend.exception.AuthenticationExceptionHandler()
                .handleBadCredentials().getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void logoutInvalidatesExistingSessionAndReturnsNoContent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = (MockHttpSession) request.getSession();
        AuthenticatedUser principal = new AuthenticatedUser(7L, "user@example.com", "hash");
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());

        assertThat(controller.logout(request, authentication).getStatusCode().value()).isEqualTo(204);
        assertThat(session.isInvalid()).isTrue();
    }
}
