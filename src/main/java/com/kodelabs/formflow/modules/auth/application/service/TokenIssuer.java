package com.kodelabs.formflow.modules.auth.application.service;

import com.kodelabs.formflow.modules.auth.domain.model.GeneratedRefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.RefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.AuthResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.RefreshTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Application-layer collaborator shared by the authentication use cases:
 * issues an access + refresh token pair and persists the refresh token hash.
 */
@Component
@RequiredArgsConstructor
public class TokenIssuer {

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final TokenServicePort tokenService;

    public AuthResult issueFor(User user, Tenant tenant) {
        String accessToken = tokenService.generateAccessToken(user);
        GeneratedRefreshToken refreshToken = tokenService.generateRefreshToken();

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .tokenHash(refreshToken.hash())
                .expiresAt(refreshToken.expiresAt())
                .build());

        return new AuthResult(accessToken, refreshToken.rawValue(),
                tokenService.accessTokenValidityMs(), user, tenant);
    }
}
