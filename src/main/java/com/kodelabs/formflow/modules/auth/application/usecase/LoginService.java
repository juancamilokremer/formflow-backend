package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.application.service.TokenIssuer;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.AuthResult;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.LoginCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.LoginUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.out.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the LoginUseCase input port.
 *
 * For security, every failure (unknown tenant, unknown user or wrong password)
 * produces the same generic message — never reveal which one failed.
 */
@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private static final String INVALID_CREDENTIALS = "Credenciales inválidas";

    private final TenantRepositoryPort tenantRepository;
    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenIssuer tokenIssuer;

    @Override
    @Transactional
    public AuthResult execute(LoginCommand command) {
        Tenant tenant = findActiveTenant(command.tenantSlug());
        User user = authenticate(command, tenant);

        return tokenIssuer.issueFor(user, tenant);
    }

    private Tenant findActiveTenant(String slug) {
        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(this::invalidCredentials);

        if (!tenant.isActive()) {
            throw new BusinessException("La empresa está suspendida o cancelada", HttpStatus.FORBIDDEN);
        }
        return tenant;
    }

    private User authenticate(LoginCommand command, Tenant tenant) {
        User user = userRepository
                .findByEmailAndTenantId(command.email().toLowerCase().trim(), tenant.getId())
                .orElseThrow(this::invalidCredentials);

        if (!user.isActive() || !passwordHasher.matches(command.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        return user;
    }

    private BusinessException invalidCredentials() {
        return new BusinessException(INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
    }
}
