package com.kodelabs.formflow.modules.auth.infrastructure.persistence.mapper;

import com.kodelabs.formflow.modules.auth.domain.model.PlanLimits;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.PlanLimitsJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PlanLimitsPersistenceMapper {

    public PlanLimits toDomain(PlanLimitsJpaEntity e) {
        return new PlanLimits(
                e.getFormsLimit(), e.getResponsesLimit(), e.getUsersLimit(),
                e.getConvocatoriasLimit(), e.isCanExportExcel());
    }

    public PlanLimitsJpaEntity toEntity(TenantPlan plan, PlanLimits limits) {
        return PlanLimitsJpaEntity.builder()
                .plan(plan)
                .formsLimit(limits.formsLimit())
                .responsesLimit(limits.responsesLimit())
                .usersLimit(limits.usersLimit())
                .convocatoriasLimit(limits.convocatoriasLimit())
                .canExportExcel(limits.canExportExcel())
                .build();
    }
}
