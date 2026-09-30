package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.UserResult;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        boolean active,
        boolean emailVerified,
        Instant createdAt
) {
    public static UserResponse from(UserResult r) {
        return new UserResponse(
                r.id(), r.email(), r.firstName(), r.lastName(), r.role(),
                r.active(), r.emailVerified(), r.createdAt());
    }
}
