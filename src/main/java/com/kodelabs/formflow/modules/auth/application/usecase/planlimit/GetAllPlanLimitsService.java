package com.kodelabs.formflow.modules.auth.application.usecase.planlimit;

import com.kodelabs.formflow.modules.auth.domain.model.PlanLimits;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetAllPlanLimitsUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.PlanLimitsResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.PlanLimitsRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetAllPlanLimitsService implements GetAllPlanLimitsUseCase {

    private final PlanLimitsRepositoryPort planLimitsRepository;

    @Override
    public List<PlanLimitsResult> execute() {
        Map<TenantPlan, PlanLimits> byPlan = planLimitsRepository.findAll();
        return byPlan.entrySet().stream()
                .map(e -> PlanLimitsResult.of(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(r -> r.plan().ordinal()))
                .toList();
    }
}
