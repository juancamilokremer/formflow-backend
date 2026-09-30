package com.kodelabs.formflow.modules.auth.application.usecase.branding;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetBrandingUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetBrandingQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetBrandingService implements GetBrandingUseCase {

    private final TenantRepositoryPort tenantRepository;

    @Override
    public BrandingResult execute(GetBrandingQuery query) {
        Tenant tenant = tenantRepository.findById(query.tenantId())
                .orElseThrow(() -> new BusinessException("error.tenant.not_found"));
        return BrandingResult.from(tenant);
    }
}
