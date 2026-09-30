package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.InvitationResult;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
        UUID id,
        String email,
        UserRole role,
        Instant expiresAt,
        Instant createdAt
) {
    public static InvitationResponse from(InvitationResult r) {
        return new InvitationResponse(r.id(), r.email(), r.role(), r.expiresAt(), r.createdAt());
    }
}
