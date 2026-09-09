package com.contactmanager.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.contactmanager.backend.entity.User;
import com.contactmanager.backend.entity.User.IdentifierType;
import com.contactmanager.backend.repository.UserRepository;

class UserAuthenticationServiceTests {

    @Test
    void normalizesEmailAndReturnsApplicationPrincipal() {
        UserRepository repository = mock(UserRepository.class);
        User user = new User("Test", "User", "user@example.com", IdentifierType.EMAIL, "hash");
        when(repository.findByIdentifier("user@example.com")).thenReturn(Optional.of(user));

        AuthenticatedUser result = (AuthenticatedUser) new UserAuthenticationService(repository)
                .loadUserByUsername(" User@Example.com ");

        assertThat(result.getUsername()).isEqualTo("user@example.com");
        assertThat(result.getPassword()).isEqualTo("hash");
        verify(repository).findByIdentifier("user@example.com");
    }

    @Test
    void unknownIdentifierReturnsGenericAuthenticationError() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.findByIdentifier("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new UserAuthenticationService(repository)
                .loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Invalid credentials");
    }
}
