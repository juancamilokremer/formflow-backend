package com.kodelabs.formflow.modules.auth.domain.port.in.result;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResult(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        boolean active,
        boolean emailVerified,
        Instant createdAt
) {
    public static UserResult from(User user) {
        return new UserResult(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getRole(), user.isActive(), user.isEmailVerified(), user.getCreatedAt());
    }
}
