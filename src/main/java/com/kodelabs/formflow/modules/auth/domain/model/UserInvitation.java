package com.kodelabs.formflow.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Invitation for a not-yet-existing user to join a tenant with a given role.
 * Only the SHA-256 hash of the token is stored, never the raw value — same discipline
 * as EmailToken.
 *
 * Pure domain POJO — no JPA/Hibernate dependencies.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInvitation {

    private UUID id;

    private UUID tenantId;

    private String email;

    private UserRole role;

    /** SHA-256 hex of the token (64 chars). */
    private String tokenHash;

    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    private UUID invitedByUserId;

    private Instant expiresAt;

    private Instant createdAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /** Usable only while still PENDING and not past its expiry. */
    public boolean isUsable() {
        return status == InvitationStatus.PENDING && !isExpired();
    }

    public void markAccepted() {
        this.status = InvitationStatus.ACCEPTED;
    }

    public void markCancelled() {
        this.status = InvitationStatus.CANCELLED;
    }
}
