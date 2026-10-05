package com.kodelabs.formflow.modules.auth.application.usecase.planlimit;

import com.kodelabs.formflow.modules.auth.domain.model.PlanLimits;
import com.kodelabs.formflow.modules.auth.domain.port.in.UpdatePlanLimitsUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdatePlanLimitsCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.PlanLimitsResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.PlanLimitsRepositoryPort;
import com.kodelabs.formflow.shared.planlimit.PlanLimitsCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePlanLimitsService implements UpdatePlanLimitsUseCase {

    private final PlanLimitsRepositoryPort planLimitsRepository;
    private final PlanLimitsCatalog planLimitsCatalog;

    @Override
    @Transactional
    public PlanLimitsResult execute(UpdatePlanLimitsCommand command) {
        PlanLimits limits = new PlanLimits(
                command.formsLimit(), command.responsesLimit(), command.usersLimit(),
                command.convocatoriasLimit(), command.canExportExcel());
        planLimitsRepository.save(command.plan(), limits);
        // Applies immediately to every tenant on this plan, not after the 1-minute
        // PlanLimitsCatalog cache TTL — same reasoning as ChangeTenantPlanService.invalidate().
        planLimitsCatalog.invalidateAll();
        return PlanLimitsResult.of(command.plan(), limits);
    }
}
