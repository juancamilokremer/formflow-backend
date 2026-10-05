package com.kodelabs.formflow.shared.planlimit;

import com.github.benmanes.caffeine.cache.Ticker;
import com.kodelabs.formflow.modules.auth.domain.model.PlanLimits;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantUsagePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanLimitServiceTest {

    private static final PlanLimits FREE_LIMITS = new PlanLimits(2, 50, 1, 0, false);
    private static final PlanLimits STARTER_LIMITS = new PlanLimits(10, 500, 3, 5, true);
    private static final PlanLimits UNLIMITED_LIMITS = new PlanLimits(-1, -1, -1, -1, true);

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private TenantUsagePort tenantUsagePort;
    @Mock private UserRepositoryPort userRepository;
    @Mock private PlanLimitsCatalog planLimitsCatalog;

    private final AtomicLong nanos = new AtomicLong();
    private final Ticker testTicker = nanos::get;
    private PlanLimitService service;

    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new PlanLimitService(tenantRepository, tenantUsagePort, userRepository, planLimitsCatalog, testTicker);
    }

    private void stubTenant(TenantPlan plan) {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(Tenant.builder().id(tenantId).plan(plan).build()));
        when(planLimitsCatalog.forPlan(plan)).thenReturn(limitsFor(plan));
    }

    private PlanLimits limitsFor(TenantPlan plan) {
        return switch (plan) {
            case FREE -> FREE_LIMITS;
            case STARTER -> STARTER_LIMITS;
            case PRO, ENTERPRISE -> UNLIMITED_LIMITS;
        };
    }

    @Test
    void allowsCreationUnderTheFormLimit() {
        stubTenant(TenantPlan.FREE);
        when(tenantUsagePort.countForms(tenantId)).thenReturn(1L);

        service.checkFormLimit(tenantId); // FREE limit is 2, no throw
    }

    @Test
    void rejectsAtTheFormLimitWithA402AndTheThreeMessageArgs() {
        stubTenant(TenantPlan.FREE);
        when(tenantUsagePort.countForms(tenantId)).thenReturn(2L);

        assertThatThrownBy(() -> service.checkFormLimit(tenantId))
                .isInstanceOf(PlanLimitExceededException.class)
                .hasMessage("error.plan_limit.forms")
                .satisfies(ex -> {
                    var e = (PlanLimitExceededException) ex;
                    assertThat(e.getStatus().value()).isEqualTo(402);
                    assertThat(e.getArgs()).containsExactly(2, TenantPlan.FREE, TenantPlan.STARTER);
                });
    }

    @Test
    void rejectsAtTheMonthlyResponseLimit() {
        stubTenant(TenantPlan.FREE);
        when(tenantUsagePort.countResponsesThisMonth(eq(tenantId), any(), any())).thenReturn(50L);

        assertThatThrownBy(() -> service.checkMonthlyResponseLimit(tenantId))
                .isInstanceOf(PlanLimitExceededException.class)
                .hasMessage("error.plan_limit.responses");
    }

    @Test
    void rejectsAtTheUserLimit() {
        stubTenant(TenantPlan.FREE);
        when(userRepository.countByTenantIdAndActiveTrueAndRoleNot(tenantId, UserRole.SUPER_ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.checkUserLimit(tenantId))
                .isInstanceOf(PlanLimitExceededException.class)
                .hasMessage("error.plan_limit.users");
    }

    @Test
    void freePlanHasZeroConvocatoriasAvailable() {
        stubTenant(TenantPlan.FREE);
        when(tenantUsagePort.countConvocatorias(tenantId)).thenReturn(0L);

        assertThatThrownBy(() -> service.checkConvocatoriaLimit(tenantId))
                .isInstanceOf(PlanLimitExceededException.class)
                .hasMessage("error.plan_limit.convocatorias");
    }

    @Test
    void unlimitedPlanNeverThrowsRegardlessOfVolume() {
        stubTenant(TenantPlan.PRO);
        when(tenantUsagePort.countForms(tenantId)).thenReturn(10_000L);

        service.checkFormLimit(tenantId);
    }

    @Test
    void suggestsProWhenAlreadyOnStarter() {
        stubTenant(TenantPlan.STARTER);
        when(tenantUsagePort.countForms(tenantId)).thenReturn(10L);

        assertThatThrownBy(() -> service.checkFormLimit(tenantId))
                .isInstanceOf(PlanLimitExceededException.class)
                .satisfies(ex -> assertThat(((PlanLimitExceededException) ex).getArgs())
                        .containsExactly(10, TenantPlan.STARTER, TenantPlan.PRO));
    }

    @Test
    void canExportExcelReflectsThePlan() {
        stubTenant(TenantPlan.FREE);

        assertThat(service.canExportExcel(tenantId)).isFalse();
    }

    @Test
    void reusesTheCachedSnapshotWithinTheTtl() {
        stubTenant(TenantPlan.FREE);
        when(tenantUsagePort.countForms(tenantId)).thenReturn(0L);

        service.checkFormLimit(tenantId);
        service.checkFormLimit(tenantId);

        verify(tenantRepository, times(1)).findById(tenantId);
    }

    @Test
    void refreshesTheSnapshotAfterTheTtlExpires() {
        stubTenant(TenantPlan.FREE);
        when(tenantUsagePort.countForms(tenantId)).thenReturn(0L);

        service.checkFormLimit(tenantId);
        nanos.addAndGet(java.time.Duration.ofMinutes(2).toNanos());
        service.checkFormLimit(tenantId);

        verify(tenantRepository, times(2)).findById(tenantId);
    }

    @Test
    void invalidateForcesAnImmediateRefresh() {
        stubTenant(TenantPlan.FREE);
        when(tenantUsagePort.countForms(tenantId)).thenReturn(0L);

        service.checkFormLimit(tenantId);
        service.invalidate(tenantId);
        service.checkFormLimit(tenantId);

        verify(tenantRepository, times(2)).findById(tenantId);
    }
}
