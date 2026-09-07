package com.contactmanager.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import com.contactmanager.backend.dto.UserProfileResponse;

class AuthenticatedSessionServiceTests {

    @Test
    void authenticatesNormalizesIdentifierSavesSessionContextAndReturnsProfile() {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        SessionAuthenticationStrategy sessionAuthenticationStrategy = mock(SessionAuthenticationStrategy.class);
        SecurityContextRepository contextRepository = mock(SecurityContextRepository.class);
        UserProfileService profileService = mock(UserProfileService.class);
        AuthenticatedSessionService service = new AuthenticatedSessionService(
                authenticationManager, sessionAuthenticationStrategy, contextRepository, profileService);
        AuthenticatedUser principal = new AuthenticatedUser(7L, "user@example.com", "hash");
        Authentication authenticated = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authenticated);
        UserProfileResponse profile = new UserProfileResponse(7L, "Test", "User", "user@example.com", null);
        when(profileService.getProfile(7L)).thenReturn(profile);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserProfileResponse result = service.authenticate(
                " User@Example.com ", "password", request, response);

        assertThat(result).isEqualTo(profile);
        verify(authenticationManager).authenticate(eq(
                UsernamePasswordAuthenticationToken.unauthenticated("user@example.com", "password")));
        verify(sessionAuthenticationStrategy).onAuthentication(eq(authenticated), eq(request), eq(response));
        verify(contextRepository).saveContext(any(), eq(request), eq(response));
        verify(profileService).getProfile(7L);
    }
}
