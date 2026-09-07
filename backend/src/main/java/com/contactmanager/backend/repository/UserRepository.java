package com.contactmanager.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.contactmanager.backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByIdentifier(String identifier);

    Optional<User> findByIdentifier(String identifier);

    List<User> findAllByIdentifier(String identifier);
}
