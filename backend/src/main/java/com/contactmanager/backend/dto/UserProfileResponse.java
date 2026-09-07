package com.contactmanager.backend.dto;

import com.contactmanager.backend.entity.User;

public record UserProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone) {

    public static UserProfileResponse from(User user) {
        boolean email = user.getIdentifierType() == User.IdentifierType.EMAIL;
        return new UserProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                email ? user.getIdentifier() : null,
                email ? null : user.getIdentifier());
    }
}
