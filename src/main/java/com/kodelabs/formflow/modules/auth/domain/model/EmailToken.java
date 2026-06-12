package com.kodelabs.formflow.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Single-use token sent by email (password reset / email verification).
 * Only the SHA-256 hash is stored, never the raw value.
 *
 * Pure domain POJO — no JPA/Hibernate dependencies.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailToken {

    private UUID id;

    private UUID userId;

    private UUID tenantId;

    /** SHA-256 hex of the token (64 chars). */
    private String tokenHash;

    private EmailTokenType type;

    private Instant expiresAt;

    private Instant usedAt;

    private Instant createdAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    /** A token is usable only if it has not expired nor been consumed. */
    public boolean isUsable() {
        return !isExpired() && !isUsed();
    }

    public void markUsed() {
        this.usedAt = Instant.now();
    }
}
