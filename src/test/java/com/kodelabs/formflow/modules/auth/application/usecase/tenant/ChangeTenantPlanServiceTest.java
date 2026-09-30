package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ChangeTenantPlanCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.planlimit.PlanLimitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeTenantPlanServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private PlanLimitService planLimitService;
    @InjectMocks private ChangeTenantPlanService service;

    @Test
    void changesThePlan() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(tenantId).plan(TenantPlan.FREE).build();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(tenant)).thenReturn(tenant);

        TenantResult result = service.execute(new ChangeTenantPlanCommand(tenantId, TenantPlan.PRO));

        assertThat(result.plan()).isEqualTo(TenantPlan.PRO);
    }

    @Test
    void invalidatesThePlanLimitCacheSoNewLimitsApplyImmediately() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(tenantId).plan(TenantPlan.FREE).build();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(tenant)).thenReturn(tenant);

        service.execute(new ChangeTenantPlanCommand(tenantId, TenantPlan.PRO));

        verify(planLimitService).invalidate(tenantId);
    }
}
