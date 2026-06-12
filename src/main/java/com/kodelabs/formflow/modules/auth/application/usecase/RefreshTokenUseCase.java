package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.domain.model.GeneratedRefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.RefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.RefreshTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Refresh token rotation (single use): validates the incoming token,
 * revokes it and issues a new access + refresh pair.
 *
 * Reuse detection: if an already-revoked token arrives, theft is assumed
 * and ALL active tokens of the user are revoked.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private static final String INVALID_TOKEN = "Refresh token inválido o expirado";

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final UserRepositoryPort userRepository;
    private final TenantRepositoryPort tenantRepository;
    private final TokenServicePort tokenService;

    @Transactional
    public AuthResult execute(RefreshTokenCommand command) {
        String hash = tokenService.hashRefreshToken(command.refreshToken());

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(this::invalidToken);

        if (stored.isRevoked()) {
            log.warn("Reuse of revoked refresh token detected for user {} — revoking all their tokens",
                    stored.getUserId());
            refreshTokenRepository.revokeAllByUserId(stored.getUserId());
            throw invalidToken();
        }

        if (stored.isExpired()) {
            throw invalidToken();
        }

        User user = userRepository.findByIdAndTenantId(stored.getUserId(), stored.getTenantId())
                .filter(User::isActive)
                .orElseThrow(this::invalidToken);

        Tenant tenant = tenantRepository.findById(stored.getTenantId())
                .filter(Tenant::isActive)
                .orElseThrow(this::invalidToken);

        // Rotation: the used token gets revoked and a new one is issued
        stored.revoke();
        refreshTokenRepository.save(stored);

        String accessToken = tokenService.generateAccessToken(user);
        GeneratedRefreshToken newRefreshToken = tokenService.generateRefreshToken();

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .tokenHash(newRefreshToken.hash())
                .expiresAt(newRefreshToken.expiresAt())
                .build());

        return new AuthResult(accessToken, newRefreshToken.rawValue(),
                tokenService.accessTokenValidityMs(), user, tenant);
    }

    private BusinessException invalidToken() {
        return new BusinessException(INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
    }
}
