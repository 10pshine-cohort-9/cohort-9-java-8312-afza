package com.contactmanager.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.contactmanager.backend.dto.ChangePasswordRequest;
import com.contactmanager.backend.dto.UserProfileResponse;
import com.contactmanager.backend.entity.User;
import com.contactmanager.backend.exception.InvalidCurrentPasswordException;
import com.contactmanager.backend.repository.UserRepository;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        return UserProfileResponse.from(requireUser(userId));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = requireUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException();
        }
        user.changePasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }
}
