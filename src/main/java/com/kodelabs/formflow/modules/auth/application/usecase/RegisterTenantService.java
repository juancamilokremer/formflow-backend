package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.application.service.TokenIssuer;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.AuthResult;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.RegisterTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.RegisterTenantUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.out.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the RegisterTenantUseCase input port.
 */
@Service
@RequiredArgsConstructor
public class RegisterTenantService implements RegisterTenantUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenIssuer tokenIssuer;

    @Override
    @Transactional
    public AuthResult execute(RegisterTenantCommand command) {
        ensureSlugIsAvailable(command.slug());

        Tenant tenant = createTenant(command);
        User admin = createAdminUser(command, tenant);

        return tokenIssuer.issueFor(admin, tenant);
    }

    private void ensureSlugIsAvailable(String slug) {
        if (tenantRepository.existsBySlug(slug)) {
            throw new BusinessException(
                    "Ya existe una empresa registrada con el identificador '" + slug + "'",
                    HttpStatus.CONFLICT);
        }
    }

    private Tenant createTenant(RegisterTenantCommand command) {
        return tenantRepository.save(Tenant.builder()
                .slug(command.slug())
                .name(command.companyName())
                .build());
    }

    private User createAdminUser(RegisterTenantCommand command, Tenant tenant) {
        return userRepository.save(User.builder()
                .tenantId(tenant.getId())
                .email(command.email().toLowerCase().trim())
                .passwordHash(passwordHasher.hash(command.password()))
                .firstName(command.firstName())
                .lastName(command.lastName())
                .role(UserRole.TENANT_ADMIN)
                .build());
    }
}
