package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.application.service.TokenIssuer;
import com.kodelabs.formflow.modules.auth.domain.model.RefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.AuthResult;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.RefreshTokenCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.RefreshTokenUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.out.RefreshTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the RefreshTokenUseCase input port.
 *
 * Reuse detection: if an already-revoked token arrives, theft is assumed
 * and ALL active tokens of the user are revoked.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private static final String INVALID_TOKEN = "Refresh token inválido o expirado";

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final UserRepositoryPort userRepository;
    private final TenantRepositoryPort tenantRepository;
    private final TokenServicePort tokenService;
    private final TokenIssuer tokenIssuer;

    @Override
    @Transactional
    public AuthResult execute(RefreshTokenCommand command) {
        RefreshToken stored = findStoredToken(command.refreshToken());
        ensureUsable(stored);

        User user = loadActiveUser(stored);
        Tenant tenant = loadActiveTenant(stored);

        rotate(stored);
        return tokenIssuer.issueFor(user, tenant);
    }

    private RefreshToken findStoredToken(String rawToken) {
        String hash = tokenService.hashRefreshToken(rawToken);
        return refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(this::invalidToken);
    }

    private void ensureUsable(RefreshToken stored) {
        if (stored.isRevoked()) {
            log.warn("Reuse of revoked refresh token detected for user {} — revoking all their tokens",
                    stored.getUserId());
            refreshTokenRepository.revokeAllByUserId(stored.getUserId());
            throw invalidToken();
        }
        if (stored.isExpired()) {
            throw invalidToken();
        }
    }

    private User loadActiveUser(RefreshToken stored) {
        return userRepository.findByIdAndTenantId(stored.getUserId(), stored.getTenantId())
                .filter(User::isActive)
                .orElseThrow(this::invalidToken);
    }

    private Tenant loadActiveTenant(RefreshToken stored) {
        return tenantRepository.findById(stored.getTenantId())
                .filter(Tenant::isActive)
                .orElseThrow(this::invalidToken);
    }

    /** Single-use rotation: the incoming token is revoked before issuing a new pair. */
    private void rotate(RefreshToken stored) {
        stored.revoke();
        refreshTokenRepository.save(stored);
    }

    private BusinessException invalidToken() {
        return new BusinessException(INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
    }
}
