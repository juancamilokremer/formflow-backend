package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.application.service.TokenIssuer;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.LoginUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.LoginCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.AuthResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the LoginUseCase input port.
 *
 * For security, every failure (unknown tenant, unknown user or wrong password)
 * produces the same generic message — never reveal which one failed.
 * The real reason IS logged (WARN) as support evidence and brute-force signal.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private static final String INVALID_CREDENTIALS = "error.auth.invalid_credentials";

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
                .orElseThrow(() -> invalidCredentials("unknown tenant slug '" + slug + "'"));

        if (!tenant.isActive()) {
            log.warn("Login rejected: tenant '{}' is {}", slug, tenant.getStatus());
            throw new BusinessException("error.tenant.suspended", HttpStatus.FORBIDDEN);
        }
        return tenant;
    }

    private User authenticate(LoginCommand command, Tenant tenant) {
        String email = command.email().toLowerCase().trim();
        User user = userRepository.findByEmailAndTenantId(email, tenant.getId())
                .orElseThrow(() -> invalidCredentials(
                        "unknown email '" + email + "' on tenant '" + tenant.getSlug() + "'"));

        if (!user.isActive() || !passwordHasher.matches(command.password(), user.getPasswordHash())) {
            throw invalidCredentials(
                    "wrong password or inactive user for '" + email + "' on tenant '" + tenant.getSlug() + "'");
        }

        if (!user.isEmailVerified()) {
            log.warn("Login rejected: email not verified for '{}' on tenant '{}'", email, tenant.getSlug());
            throw new BusinessException("error.auth.email_not_verified", HttpStatus.FORBIDDEN);
        }

        return user;
    }

    /** The HTTP response is always the same generic 401; the reason only goes to the log. */
    private BusinessException invalidCredentials(String reason) {
        log.warn("Login failed: {}", reason);
        return new BusinessException(INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
    }
}
