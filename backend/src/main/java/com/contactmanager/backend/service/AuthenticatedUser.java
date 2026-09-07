package com.contactmanager.backend.service;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UserDetails;

public final class AuthenticatedUser implements UserDetails, CredentialsContainer {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String identifier;
    private String passwordHash;

    public AuthenticatedUser(Long userId, String identifier, String passwordHash) {
        this.userId = userId;
        this.identifier = identifier;
        this.passwordHash = passwordHash;
    }

    public Long userId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return identifier;
    }

    @Override
    public void eraseCredentials() {
        passwordHash = null;
    }
}
