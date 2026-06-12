package com.kodelabs.formflow.modules.auth.domain.port;

import com.kodelabs.formflow.modules.auth.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para operaciones de persistencia de RefreshToken.
 */
public interface RefreshTokenRepositoryPort {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Revoca todos los tokens activos de un usuario (logout global / password reset). */
    void revokeAllByUserId(UUID userId);

    /** Limpieza de tokens expirados (job programado). */
    void deleteAllExpired();
}
