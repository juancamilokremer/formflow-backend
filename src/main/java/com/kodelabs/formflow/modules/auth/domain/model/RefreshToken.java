package com.kodelabs.formflow.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A user's refresh token. Only the SHA-256 hash of the token is stored,
 * never the raw value. Single use: rotating it marks it as revoked.
 *
 * Pure domain POJO — no JPA/Hibernate dependencies.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    private UUID id;

    private UUID userId;

    private UUID tenantId;

    /** SHA-256 hex of the token (64 chars). */
    private String tokenHash;

    private Instant expiresAt;

    private Instant revokedAt;

    private Instant createdAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    /** A token is usable only if it has not expired nor been revoked (single-use rotation). */
    public boolean isUsable() {
        return !isExpired() && !isRevoked();
    }

    public void revoke() {
        this.revokedAt = Instant.now();
    }
}
