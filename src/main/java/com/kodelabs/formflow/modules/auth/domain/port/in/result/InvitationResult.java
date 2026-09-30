package com.kodelabs.formflow.modules.auth.domain.port.in.result;

import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;

import java.time.Instant;
import java.util.UUID;

public record InvitationResult(
        UUID id,
        String email,
        UserRole role,
        Instant expiresAt,
        Instant createdAt
) {
    public static InvitationResult from(UserInvitation invitation) {
        return new InvitationResult(
                invitation.getId(), invitation.getEmail(), invitation.getRole(),
                invitation.getExpiresAt(), invitation.getCreatedAt());
    }
}
