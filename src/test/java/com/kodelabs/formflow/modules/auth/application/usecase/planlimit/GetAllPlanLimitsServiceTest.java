package com.kodelabs.formflow.modules.auth.application.usecase.planlimit;

import com.kodelabs.formflow.modules.auth.domain.model.PlanLimits;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.PlanLimitsResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.PlanLimitsRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllPlanLimitsServiceTest {

    @Mock private PlanLimitsRepositoryPort planLimitsRepository;
    @InjectMocks private GetAllPlanLimitsService service;

    @Test
    void returnsEveryPlanSortedByEnumOrder() {
        when(planLimitsRepository.findAll()).thenReturn(Map.of(
                TenantPlan.ENTERPRISE, new PlanLimits(-1, -1, -1, -1, true),
                TenantPlan.FREE, new PlanLimits(2, 50, 1, 0, false),
                TenantPlan.PRO, new PlanLimits(-1, -1, -1, -1, true),
                TenantPlan.STARTER, new PlanLimits(10, 500, 3, 5, true)));

        List<PlanLimitsResult> result = service.execute();

        assertThat(result).extracting(PlanLimitsResult::plan)
                .containsExactly(TenantPlan.FREE, TenantPlan.STARTER, TenantPlan.PRO, TenantPlan.ENTERPRISE);
    }

    @Test
    void mapsEachPlanLimitsFieldThrough() {
        when(planLimitsRepository.findAll()).thenReturn(Map.of(
                TenantPlan.FREE, new PlanLimits(2, 50, 1, 0, false)));

        PlanLimitsResult result = service.execute().get(0);

        assertThat(result.formsLimit()).isEqualTo(2);
        assertThat(result.responsesLimit()).isEqualTo(50);
        assertThat(result.usersLimit()).isEqualTo(1);
        assertThat(result.convocatoriasLimit()).isEqualTo(0);
        assertThat(result.canExportExcel()).isFalse();
    }
}
