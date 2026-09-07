package com.contactmanager.backend.dto;

public record RegistrationResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String message) {
}
