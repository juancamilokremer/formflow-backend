package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.ChangeTenantPlanUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ChangeTenantPlanCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeTenantPlanService implements ChangeTenantPlanUseCase {

    private final TenantRepositoryPort tenantRepository;

    @Override
    @Transactional
    public TenantResult execute(ChangeTenantPlanCommand command) {
        Tenant tenant = tenantRepository.findById(command.tenantId())
                .orElseThrow(() -> new BusinessException("error.tenant.not_found", HttpStatus.NOT_FOUND));
        tenant.setPlan(command.plan());
        return TenantResult.from(tenantRepository.save(tenant));
    }
}
