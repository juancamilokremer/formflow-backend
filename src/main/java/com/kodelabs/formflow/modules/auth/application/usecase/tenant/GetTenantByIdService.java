package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetTenantByIdUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetTenantByIdQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetTenantByIdService implements GetTenantByIdUseCase {

    private final TenantRepositoryPort tenantRepository;

    @Override
    public TenantResult execute(GetTenantByIdQuery query) {
        Tenant tenant = tenantRepository.findById(query.tenantId())
                .orElseThrow(() -> new BusinessException("error.tenant.not_found", HttpStatus.NOT_FOUND));
        return TenantResult.from(tenant);
    }
}
