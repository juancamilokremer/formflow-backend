package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetTenantUsageQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantUsageResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantUsagePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTenantUsageServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private TenantUsagePort tenantUsagePort;
    @InjectMocks private GetTenantUsageService service;

    private final UUID tenantId = UUID.randomUUID();

    @Test
    void combinesRealUsageWithThePlanLimits() {
        Tenant tenant = Tenant.builder().id(tenantId).plan(TenantPlan.FREE).build();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantUsagePort.countForms(tenantId)).thenReturn(2L);
        when(tenantUsagePort.countResponsesThisMonth(eq(tenantId), any(), any())).thenReturn(38L);
        when(userRepository.countByTenantIdAndActiveTrueAndRoleNot(tenantId, UserRole.SUPER_ADMIN)).thenReturn(1L);

        TenantUsageResult result = service.execute(new GetTenantUsageQuery(tenantId));

        assertThat(result.plan()).isEqualTo(TenantPlan.FREE);
        assertThat(result.formsUsed()).isEqualTo(2);
        assertThat(result.formsLimit()).isEqualTo(2);
        assertThat(result.responsesThisMonth()).isEqualTo(38);
        assertThat(result.responsesLimit()).isEqualTo(50);
        assertThat(result.usersCount()).isEqualTo(1);
        assertThat(result.usersLimit()).isEqualTo(1);
        assertThat(result.canExportExcel()).isFalse();
    }

    @Test
    void reportsUnlimitedAsANegativeSentinelForUnlimitedPlans() {
        Tenant tenant = Tenant.builder().id(tenantId).plan(TenantPlan.PRO).build();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantUsagePort.countForms(tenantId)).thenReturn(120L);
        when(tenantUsagePort.countResponsesThisMonth(eq(tenantId), any(), any())).thenReturn(9000L);
        when(userRepository.countByTenantIdAndActiveTrueAndRoleNot(tenantId, UserRole.SUPER_ADMIN)).thenReturn(4L);

        TenantUsageResult result = service.execute(new GetTenantUsageQuery(tenantId));

        assertThat(result.formsLimit()).isEqualTo(-1);
        assertThat(result.responsesLimit()).isEqualTo(-1);
        assertThat(result.usersLimit()).isEqualTo(-1);
        assertThat(result.canExportExcel()).isTrue();
    }
}
