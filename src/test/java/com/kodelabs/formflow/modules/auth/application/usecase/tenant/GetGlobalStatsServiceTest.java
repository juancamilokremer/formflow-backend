package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.GlobalStatsResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantUsagePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetGlobalStatsServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private TenantUsagePort tenantUsagePort;
    @InjectMocks private GetGlobalStatsService service;

    @Test
    void aggregatesAcrossAllTenants() {
        when(tenantRepository.countAll(null, null)).thenReturn(12L);
        when(tenantUsagePort.countAllResponsesThisMonth(any(), any())).thenReturn(340L);
        when(tenantRepository.countByPlan()).thenReturn(Map.of(TenantPlan.FREE, 8L, TenantPlan.PRO, 4L));

        GlobalStatsResult result = service.execute();

        assertThat(result.totalTenants()).isEqualTo(12);
        assertThat(result.responsesThisMonth()).isEqualTo(340);
        assertThat(result.tenantsByPlan()).containsEntry(TenantPlan.FREE, 8L);
    }
}
