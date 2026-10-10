package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.application.service.PlanUpgradeRequestEmailSender;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.RequestPlanUpgradeUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.RequestPlanUpgradeCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestPlanUpgradeService implements RequestPlanUpgradeUseCase {

    private final UserRepositoryPort userRepository;
    private final TenantRepositoryPort tenantRepository;
    private final PlanUpgradeRequestEmailSender emailSender;

    @Override
    public void execute(RequestPlanUpgradeCommand command) {
        User requester = userRepository.findByIdAndTenantId(command.userId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.user.not_found"));
        Tenant tenant = tenantRepository.findById(command.tenantId())
                .orElseThrow(() -> new BusinessException("error.tenant.not_found"));

        emailSender.send(requester, tenant, command.requestedPlan(), command.message());
    }
}
