package com.kodelabs.formflow.modules.auth.application.usecase.branding;

import com.kodelabs.formflow.modules.auth.application.service.BrandingCache;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.DeleteLogoUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.DeleteLogoCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Only clears tenant.logoUrl — does not delete the stored file. Recovering the fileId from the
 * URL would couple this use case to one specific FileStoragePort adapter's URL shape; some
 * orphaned bytes on disk is an acceptable MVP trade-off (same call already made for #158).
 */
@Service
@RequiredArgsConstructor
public class DeleteLogoService implements DeleteLogoUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final BrandingCache brandingCache;

    @Override
    @Transactional
    public BrandingResult execute(DeleteLogoCommand command) {
        Tenant tenant = tenantRepository.findById(command.tenantId())
                .orElseThrow(() -> new BusinessException("error.tenant.not_found"));

        tenant.setLogoUrl(null);
        Tenant saved = tenantRepository.save(tenant);

        brandingCache.invalidate(saved.getSlug());
        return BrandingResult.from(saved);
    }
}
