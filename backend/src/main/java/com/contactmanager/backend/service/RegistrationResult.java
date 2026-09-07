package com.contactmanager.backend.service;

import com.contactmanager.backend.dto.RegistrationResponse;

public record RegistrationResult(RegistrationResponse response, boolean created, String identifier) {
}
