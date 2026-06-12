package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.domain.model.GeneratedRefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.RefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
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
 * Registers a new company (tenant) together with its admin user
 * and returns the tokens for the initial session.
 */
@Service
@RequiredArgsConstructor
public class RegisterTenantUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final UserRepositoryPort userRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenServicePort tokenService;

    @Transactional
    public AuthResult execute(RegisterTenantCommand command) {
        if (tenantRepository.existsBySlug(command.slug())) {
            throw new BusinessException(
                    "Ya existe una empresa registrada con el identificador '" + command.slug() + "'",
                    HttpStatus.CONFLICT);
        }

        Tenant tenant = tenantRepository.save(Tenant.builder()
                .slug(command.slug())
                .name(command.companyName())
                .build());

        User admin = userRepository.save(User.builder()
                .tenantId(tenant.getId())
                .email(command.email().toLowerCase().trim())
                .passwordHash(passwordHasher.hash(command.password()))
                .firstName(command.firstName())
                .lastName(command.lastName())
                .role(UserRole.TENANT_ADMIN)
                .build());

        return issueTokens(admin, tenant);
    }

    private AuthResult issueTokens(User user, Tenant tenant) {
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
