package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.domain.model.GeneratedRefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.RefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.RefreshTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authenticates a user within its tenant (email + password + company slug).
 *
 * For security, every failure (unknown tenant, unknown user or wrong password)
 * produces the same generic message — never reveal which one failed.
 */
@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private static final String INVALID_CREDENTIALS = "Credenciales inválidas";

    private final TenantRepositoryPort tenantRepository;
    private final UserRepositoryPort userRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenServicePort tokenService;

    @Transactional
    public AuthResult execute(LoginCommand command) {
        Tenant tenant = tenantRepository.findBySlug(command.tenantSlug())
                .orElseThrow(this::invalidCredentials);

        if (!tenant.isActive()) {
            throw new BusinessException("La empresa está suspendida o cancelada", HttpStatus.FORBIDDEN);
        }

        User user = userRepository
                .findByEmailAndTenantId(command.email().toLowerCase().trim(), tenant.getId())
                .orElseThrow(this::invalidCredentials);

        if (!user.isActive() || !passwordHasher.matches(command.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

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

    private BusinessException invalidCredentials() {
        return new BusinessException(INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
    }
}
