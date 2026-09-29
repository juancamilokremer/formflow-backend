package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantStatus;
import com.kodelabs.formflow.modules.auth.domain.port.in.SuspendTenantUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.SuspendTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SuspendTenantService implements SuspendTenantUseCase {

    private final TenantRepositoryPort tenantRepository;

    @Override
    @Transactional
    public TenantResult execute(SuspendTenantCommand command) {
        Tenant tenant = tenantRepository.findById(command.tenantId())
                .orElseThrow(() -> new BusinessException("error.tenant.not_found", HttpStatus.NOT_FOUND));
        tenant.setStatus(TenantStatus.SUSPENDED);
        return TenantResult.from(tenantRepository.save(tenant));
    }
}
