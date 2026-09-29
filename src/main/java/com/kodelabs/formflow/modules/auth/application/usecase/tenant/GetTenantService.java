package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetTenantUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetTenantQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetTenantService implements GetTenantUseCase {

    private final TenantRepositoryPort tenantRepository;

    @Override
    public TenantResult execute(GetTenantQuery query) {
        Tenant tenant = tenantRepository.findById(query.tenantId())
                .orElseThrow(() -> new BusinessException("error.tenant.not_found"));
        return TenantResult.from(tenant);
    }
}
