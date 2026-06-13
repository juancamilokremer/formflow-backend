package com.kodelabs.formflow.modules.auth.domain.port.out;

import com.kodelabs.formflow.modules.auth.domain.model.GeneratedRefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.User;

/**
 * Output port for token generation.
 * The implementation (JWT + SecureRandom) lives in infrastructure —
 * use cases do not know the token format.
 */
public interface TokenServicePort {

    /** Generates the access token with userId, tenantId, email and role claims. */
    String generateAccessToken(User user);

    /** Access token validity in milliseconds (reported back to the client). */
    long accessTokenValidityMs();

    /** Generates an opaque refresh token: raw value for the client + hash to persist. */
    GeneratedRefreshToken generateRefreshToken();

    /** Generates a generic opaque token (256-bit SecureRandom, URL-safe) — email links, etc. */
    String generateOpaqueToken();

    /** Deterministic hash (SHA-256 hex, 64 chars) of an opaque token, used for DB lookup. */
    String hashToken(String rawToken);

    /** Alias of {@link #hashToken(String)} kept for refresh-token call sites. */
    default String hashRefreshToken(String rawToken) {
        return hashToken(rawToken);
    }
}
