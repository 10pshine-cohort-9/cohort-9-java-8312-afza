package com.contactmanager.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.contactmanager.backend.dto.ChangePasswordRequest;
import com.contactmanager.backend.dto.UserProfileResponse;
import com.contactmanager.backend.exception.InvalidCurrentPasswordException;
import com.contactmanager.backend.service.AuthenticatedUser;
import com.contactmanager.backend.service.UserProfileService;

class UserProfileControllerTests {

    private UserProfileService profileService;
    private UserProfileController controller;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        profileService = mock(UserProfileService.class);
        controller = new UserProfileController(profileService);
        AuthenticatedUser principal = new AuthenticatedUser(7L, "user@example.com", "hash");
        authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
    }

    @Test
    void retrievesAuthenticatedUsersProfile() {
        UserProfileResponse profile = new UserProfileResponse(7L, "Test", "User", "user@example.com", null);
        when(profileService.getProfile(7L)).thenReturn(profile);

        assertThat(controller.profile(authentication)).isEqualTo(profile);
        verify(profileService).getProfile(7L);
    }

    @Test
    void changesAuthenticatedUsersPasswordAndReturnsNoContent() {
        ChangePasswordRequest request = new ChangePasswordRequest("old-password", "new-password", "new-password");

        assertThat(controller.changePassword(authentication, request).getStatusCode().value()).isEqualTo(204);
        verify(profileService).changePassword(7L, request);
    }

    @Test
    void preservesInvalidCurrentPasswordErrorForApiHandler() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrong-password", "new-password", "new-password");
        org.mockito.Mockito.doThrow(new InvalidCurrentPasswordException())
                .when(profileService).changePassword(7L, request);

        assertThatThrownBy(() -> controller.changePassword(authentication, request))
                .isInstanceOf(InvalidCurrentPasswordException.class);
    }
}
