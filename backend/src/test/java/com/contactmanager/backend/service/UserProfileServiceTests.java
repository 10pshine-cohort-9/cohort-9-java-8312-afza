package com.contactmanager.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.contactmanager.backend.dto.ChangePasswordRequest;
import com.contactmanager.backend.dto.UserProfileResponse;
import com.contactmanager.backend.entity.User;
import com.contactmanager.backend.entity.User.IdentifierType;
import com.contactmanager.backend.exception.InvalidCurrentPasswordException;
import com.contactmanager.backend.repository.UserRepository;

class UserProfileServiceTests {

    private UserRepository repository;
    private UserProfileService service;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        encoder = new BCryptPasswordEncoder();
        service = new UserProfileService(repository, encoder);
    }

    @Test
    void profileReturnsNoPasswordData() {
        User user = new User("Test", "User", "user@example.com", IdentifierType.EMAIL,
                encoder.encode("old-password"));
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse profile = service.getProfile(1L);

        assertThat(profile.email()).isEqualTo("user@example.com");
        assertThat(profile.phone()).isNull();
    }

    @Test
    void changesPasswordOnlyWhenCurrentPasswordMatches() {
        User user = new User("Test", "User", "+923001234567", IdentifierType.PHONE,
                encoder.encode("old-password"));
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        service.changePassword(1L,
                new ChangePasswordRequest("old-password", "new-password", "new-password"));

        assertThat(encoder.matches("new-password", user.getPasswordHash())).isTrue();
        verify(repository).save(any(User.class));
    }

    @Test
    void rejectsIncorrectCurrentPassword() {
        User user = new User("Test", "User", "user@example.com", IdentifierType.EMAIL,
                encoder.encode("old-password"));
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.changePassword(1L,
                new ChangePasswordRequest("wrong-password", "new-password", "new-password")))
                .isInstanceOf(InvalidCurrentPasswordException.class);
    }
}
