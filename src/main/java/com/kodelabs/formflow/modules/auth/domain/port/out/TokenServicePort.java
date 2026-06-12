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

    /** Deterministic hash (SHA-256 hex) of an incoming refresh token, used for DB lookup. */
    String hashRefreshToken(String rawToken);
}
