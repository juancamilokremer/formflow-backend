package com.kodelabs.formflow.modules.auth.domain.port.out;

import com.kodelabs.formflow.modules.auth.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port for RefreshToken persistence operations.
 */
public interface RefreshTokenRepositoryPort {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Revokes every active token of a user (global logout / password reset). */
    void revokeAllByUserId(UUID userId);

    /** Cleanup of expired tokens (scheduled job). */
    void deleteAllExpired();
}
