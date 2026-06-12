package com.kodelabs.formflow.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Generation and validation of JWT access tokens (HMAC, jjwt 0.12.x).
 * The token carries the claims: userId (subject), tenantId, email and role.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String CLAIM_TENANT_ID = "tenantId";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLE = "role";

    private final JwtProperties properties;

    public String generateAccessToken(UUID userId, UUID tenantId, String email, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_TENANT_ID, tenantId.toString())
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + properties.getExpirationMs()))
                .signWith(signingKey())
                .compact();
    }

    /**
     * Validates signature and expiration. Returns the claims when the token
     * is valid, Optional.empty() when it is invalid or expired.
     */
    public Optional<Claims> parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public long getAccessTokenValidityMs() {
        return properties.getExpirationMs();
    }

    public long getRefreshTokenValidityMs() {
        return properties.getRefreshExpirationMs();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
