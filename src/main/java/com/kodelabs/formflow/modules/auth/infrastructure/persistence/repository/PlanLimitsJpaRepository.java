package com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.PlanLimitsJpaEntity;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface PlanLimitsJpaRepository extends Repository<PlanLimitsJpaEntity, TenantPlan> {

    Optional<PlanLimitsJpaEntity> findByPlan(TenantPlan plan);

    List<PlanLimitsJpaEntity> findAll();

    PlanLimitsJpaEntity save(PlanLimitsJpaEntity entity);
}
