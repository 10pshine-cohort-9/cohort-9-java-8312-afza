package com.contactmanager.backend.service;

import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.contactmanager.backend.entity.User;
import com.contactmanager.backend.repository.UserRepository;

@Service
public class UserAuthenticationService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserAuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String suppliedIdentifier) throws UsernameNotFoundException {
        String identifier = normalizeIdentifier(suppliedIdentifier);
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        return new AuthenticatedUser(user.getId(), user.getIdentifier(), user.getPasswordHash());
    }

    public static String normalizeIdentifier(String identifier) {
        String normalized = identifier == null ? "" : identifier.trim();
        return normalized.contains("@") ? normalized.toLowerCase(Locale.ROOT) : normalized;
    }
}
