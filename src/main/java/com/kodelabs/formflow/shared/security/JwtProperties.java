package com.kodelabs.formflow.shared.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT configuration properties (app.jwt prefix in application.yml).
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    /** Minimum length for HS256 to be cryptographically sound (jjwt's own floor). */
    private static final int MIN_SECRET_LENGTH = 32;

    /** HMAC secret used to sign tokens. Minimum 32 characters, 64+ recommended in production. */
    private String secret;

    /** Access token validity in milliseconds. */
    private long expirationMs;

    /** Refresh token validity in milliseconds. */
    private long refreshExpirationMs;

    /** Fails the app at startup instead of letting a too-short/missing secret reach
     *  {@code Keys.hmacShaKeyFor} only on the first login attempt — or, worse, silently
     *  signing every token with the dev-convenience placeholder in application.yml if
     *  JWT_SECRET was never set at all (application-prod.yml has no fallback for it). */
    @PostConstruct
    void validateSecretLength() {
        if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "app.jwt.secret (JWT_SECRET) must be set to a random string of at least "
                            + MIN_SECRET_LENGTH + " characters.");
        }
    }
}
