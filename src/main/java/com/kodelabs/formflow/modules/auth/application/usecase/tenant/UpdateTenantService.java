package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.UpdateTenantUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdateTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.sanitize.HtmlSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateTenantService implements UpdateTenantUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final HtmlSanitizer htmlSanitizer;

    @Override
    @Transactional
    public TenantResult execute(UpdateTenantCommand command) {
        Tenant tenant = tenantRepository.findById(command.tenantId())
                .orElseThrow(() -> new BusinessException("error.tenant.not_found"));

        if (command.slug() != null && !command.slug().equals(tenant.getSlug())) {
            throw new BusinessException("error.tenant.slug_immutable", HttpStatus.BAD_REQUEST);
        }

        tenant.setName(htmlSanitizer.sanitize(command.name()));
        tenant.setLogoUrl(command.logoUrl());
        tenant.setPrimaryColor(command.primaryColor());
        tenant.setSecondaryColor(command.secondaryColor());

        return TenantResult.from(tenantRepository.save(tenant));
    }
}
