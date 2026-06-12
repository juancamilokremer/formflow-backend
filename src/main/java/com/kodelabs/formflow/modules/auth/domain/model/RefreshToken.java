package com.kodelabs.formflow.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Refresh token de un usuario. Se almacena solo el hash SHA-256 del token,
 * nunca el valor en claro. Uso único: al rotarlo se marca como revocado.
 *
 * POJO puro de dominio — sin dependencias de JPA/Hibernate.
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

    private String tokenHash; // SHA-256 hex del token (64 chars)

    private Instant expiresAt;

    private Instant revokedAt;

    private Instant createdAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    /** Un token es usable solo si no expiró y no fue revocado (rotación de uso único). */
    public boolean isUsable() {
        return !isExpired() && !isRevoked();
    }

    public void revoke() {
        this.revokedAt = Instant.now();
    }
}
