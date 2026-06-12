package com.kodelabs.formflow.modules.auth.infrastructure.web.dto;

import com.kodelabs.formflow.modules.auth.domain.model.User;

import java.util.UUID;

/**
 * Basic user data returned by the authentication endpoints.
 */
public record UserSummary(UUID id, String email, String fullName, String role) {

    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getEmail(), user.getFullName(), user.getRole().name());
    }
}
