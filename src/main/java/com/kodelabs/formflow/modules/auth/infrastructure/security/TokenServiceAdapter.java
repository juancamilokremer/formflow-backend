package com.kodelabs.formflow.modules.auth.infrastructure.security;

import com.kodelabs.formflow.modules.auth.domain.model.GeneratedRefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import com.kodelabs.formflow.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Token adapter: JWT access token (delegated to JwtService) and opaque
 * refresh token (256-bit SecureRandom, persisted as SHA-256 hex).
 */
@Component
@RequiredArgsConstructor
public class TokenServiceAdapter implements TokenServicePort {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int REFRESH_TOKEN_BYTES = 32;

    private final JwtService jwtService;

    @Override
    public String generateAccessToken(User user) {
        return jwtService.generateAccessToken(
                user.getId(), user.getTenantId(), user.getEmail(), user.getRole().name());
    }

    @Override
    public long accessTokenValidityMs() {
        return jwtService.getAccessTokenValidityMs();
    }

    @Override
    public GeneratedRefreshToken generateRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        String rawValue = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = Instant.now().plusMillis(jwtService.getRefreshTokenValidityMs());
        return new GeneratedRefreshToken(rawValue, hashRefreshToken(rawValue), expiresAt);
    }

    @Override
    public String hashRefreshToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available in this JVM", ex);
        }
    }
}
