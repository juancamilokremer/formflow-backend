package com.kodelabs.formflow.modules.auth.application.usecase.planlimit;

import com.kodelabs.formflow.modules.auth.domain.model.PlanLimits;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdatePlanLimitsCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.PlanLimitsResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.PlanLimitsRepositoryPort;
import com.kodelabs.formflow.shared.planlimit.PlanLimitsCatalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class UpdatePlanLimitsServiceTest {

    @Mock private PlanLimitsRepositoryPort planLimitsRepository;
    @Mock private PlanLimitsCatalog planLimitsCatalog;
    @InjectMocks private UpdatePlanLimitsService service;

    @Test
    void savesTheNewLimitsAndInvalidatesTheCache() {
        var command = new UpdatePlanLimitsCommand(TenantPlan.STARTER, 20, 1000, 5, 10, true);

        PlanLimitsResult result = service.execute(command);

        verify(planLimitsRepository).save(TenantPlan.STARTER, new PlanLimits(20, 1000, 5, 10, true));
        verify(planLimitsCatalog).invalidateAll();
        assertThat(result.plan()).isEqualTo(TenantPlan.STARTER);
        assertThat(result.formsLimit()).isEqualTo(20);
        assertThat(result.responsesLimit()).isEqualTo(1000);
        assertThat(result.usersLimit()).isEqualTo(5);
        assertThat(result.convocatoriasLimit()).isEqualTo(10);
        assertThat(result.canExportExcel()).isTrue();
    }

    @Test
    void preservesTheUnlimitedSentinel() {
        var command = new UpdatePlanLimitsCommand(TenantPlan.PRO, -1, -1, -1, -1, true);

        service.execute(command);

        verify(planLimitsRepository).save(TenantPlan.PRO, new PlanLimits(-1, -1, -1, -1, true));
        verify(planLimitsCatalog).invalidateAll();
        verifyNoMoreInteractions(planLimitsRepository, planLimitsCatalog);
    }
}
