package com.kodelabs.formflow.modules.auth.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.auth.domain.model.PlanLimits;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.port.out.PlanLimitsRepositoryPort;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.PlanLimitsJpaEntity;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.mapper.PlanLimitsPersistenceMapper;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository.PlanLimitsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlanLimitsRepositoryAdapter implements PlanLimitsRepositoryPort {

    private final PlanLimitsJpaRepository planLimitsJpa;
    private final PlanLimitsPersistenceMapper mapper;

    @Override
    public PlanLimits findByPlan(TenantPlan plan) {
        return planLimitsJpa.findByPlan(plan)
                .map(mapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("No plan_limits row seeded for plan " + plan));
    }

    @Override
    public Map<TenantPlan, PlanLimits> findAll() {
        return planLimitsJpa.findAll().stream()
                .collect(Collectors.toMap(PlanLimitsJpaEntity::getPlan, mapper::toDomain));
    }

    @Override
    public void save(TenantPlan plan, PlanLimits limits) {
        planLimitsJpa.save(mapper.toEntity(plan, limits));
    }
}
