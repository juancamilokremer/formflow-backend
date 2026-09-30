package com.kodelabs.formflow.modules.auth.application.usecase.branding;

import com.kodelabs.formflow.modules.auth.application.service.BrandingCache;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.UpdateBrandingUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdateBrandingCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateBrandingService implements UpdateBrandingUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final BrandingCache brandingCache;

    @Override
    @Transactional
    public BrandingResult execute(UpdateBrandingCommand command) {
        Tenant tenant = tenantRepository.findById(command.tenantId())
                .orElseThrow(() -> new BusinessException("error.tenant.not_found"));

        tenant.setName(command.name());
        tenant.setPrimaryColor(command.primaryColor());
        tenant.setSecondaryColor(command.secondaryColor());
        Tenant saved = tenantRepository.save(tenant);

        brandingCache.invalidate(saved.getSlug());
        return BrandingResult.from(saved);
    }
}
