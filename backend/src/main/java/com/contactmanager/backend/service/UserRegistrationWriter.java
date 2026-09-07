package com.contactmanager.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.contactmanager.backend.entity.User;
import com.contactmanager.backend.repository.UserRepository;

@Service
public class UserRegistrationWriter {

    private final UserRepository userRepository;

    public UserRegistrationWriter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insert(User user) {
        userRepository.saveAndFlush(user);
    }
}
