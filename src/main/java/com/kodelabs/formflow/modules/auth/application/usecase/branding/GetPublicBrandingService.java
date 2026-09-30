package com.kodelabs.formflow.modules.auth.application.usecase.branding;

import com.kodelabs.formflow.modules.auth.application.service.BrandingCache;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetPublicBrandingUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetPublicBrandingQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPublicBrandingService implements GetPublicBrandingUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final BrandingCache brandingCache;

    @Override
    public BrandingResult execute(GetPublicBrandingQuery query) {
        return brandingCache.get(query.tenantSlug()).orElseGet(() -> loadAndCache(query.tenantSlug()));
    }

    private BrandingResult loadAndCache(String slug) {
        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException("error.tenant.not_found", HttpStatus.NOT_FOUND));
        BrandingResult result = BrandingResult.from(tenant);
        brandingCache.put(slug, result);
        return result;
    }
}
