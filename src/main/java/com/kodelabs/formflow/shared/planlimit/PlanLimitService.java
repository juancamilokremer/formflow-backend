package com.kodelabs.formflow.shared.planlimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.kodelabs.formflow.modules.auth.domain.model.PlanLimits;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantUsagePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Central plan-limit enforcement (backend#6). Explicit checks in use cases, not AOP — a plan
 * limit is a business rule with a specific 402 message, not a generic cross-cutting concern.
 *
 * One UsageSnapshot per tenant, cached 1 minute (Caffeine, no Spring bean — reuses the
 * com.github.ben-manes.caffeine:caffeine library already on the classpath transitively via the
 * :jcache artifact added for backend#165, without touching bucket4j's JCache setup or
 * reintroducing Spring's own cache abstraction, deliberately removed in #165). A plan change
 * (ChangeTenantPlanService) calls invalidate() for immediate effect instead of waiting out the TTL.
 */
@Service
public class PlanLimitService {

    private final TenantRepositoryPort tenantRepository;
    private final TenantUsagePort tenantUsagePort;
    private final UserRepositoryPort userRepository;
    private final Cache<UUID, UsageSnapshot> cache;

    @Autowired
    public PlanLimitService(TenantRepositoryPort tenantRepository, TenantUsagePort tenantUsagePort,
                             UserRepositoryPort userRepository) {
        this(tenantRepository, tenantUsagePort, userRepository, Ticker.systemTicker());
    }

    /** Package-private: lets PlanLimitServiceTest advance a fake ticker to prove the TTL works. */
    PlanLimitService(TenantRepositoryPort tenantRepository, TenantUsagePort tenantUsagePort,
                      UserRepositoryPort userRepository, Ticker ticker) {
        this.tenantRepository = tenantRepository;
        this.tenantUsagePort = tenantUsagePort;
        this.userRepository = userRepository;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .ticker(ticker)
                .build();
    }

    public void checkFormLimit(UUID tenantId) {
        UsageSnapshot usage = snapshotFor(tenantId);
        PlanLimits limits = PlanLimits.forPlan(usage.plan());
        assertUnderLimit(usage.formsUsed(), limits.formsLimit(), "error.plan_limit.forms", usage.plan());
    }

    public void checkMonthlyResponseLimit(UUID tenantId) {
        UsageSnapshot usage = snapshotFor(tenantId);
        PlanLimits limits = PlanLimits.forPlan(usage.plan());
        assertUnderLimit(usage.responsesThisMonth(), limits.responsesLimit(), "error.plan_limit.responses", usage.plan());
    }

    /** Implemented and tested per backend#6; not yet called from any use case — there is no
     *  user-invitation flow to gate (backend#8 hasn't been built). */
    public void checkUserLimit(UUID tenantId) {
        UsageSnapshot usage = snapshotFor(tenantId);
        PlanLimits limits = PlanLimits.forPlan(usage.plan());
        assertUnderLimit(usage.usersCount(), limits.usersLimit(), "error.plan_limit.users", usage.plan());
    }

    public void checkConvocatoriaLimit(UUID tenantId) {
        UsageSnapshot usage = snapshotFor(tenantId);
        PlanLimits limits = PlanLimits.forPlan(usage.plan());
        assertUnderLimit(usage.convocatoriasUsed(), limits.convocatoriasLimit(), "error.plan_limit.convocatorias", usage.plan());
    }

    public boolean canExportExcel(UUID tenantId) {
        return PlanLimits.forPlan(snapshotFor(tenantId).plan()).canExportExcel();
    }

    public void invalidate(UUID tenantId) {
        cache.invalidate(tenantId);
    }

    private void assertUnderLimit(long used, int limit, String messageKey, TenantPlan currentPlan) {
        if (limit < 0) return; // unlimited
        if (used >= limit) {
            throw new PlanLimitExceededException(messageKey, limit, currentPlan, nextPlan(currentPlan));
        }
    }

    private TenantPlan nextPlan(TenantPlan plan) {
        return switch (plan) {
            case FREE -> TenantPlan.STARTER;
            default -> TenantPlan.PRO;
        };
    }

    private UsageSnapshot snapshotFor(UUID tenantId) {
        UsageSnapshot cached = cache.getIfPresent(tenantId);
        if (cached != null) return cached;

        UsageSnapshot fresh = loadUsage(tenantId);
        cache.put(tenantId, fresh);
        return fresh;
    }

    private UsageSnapshot loadUsage(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException("error.tenant.not_found", HttpStatus.NOT_FOUND));

        YearMonth currentMonth = YearMonth.now(ZoneOffset.UTC);
        Instant monthStart = currentMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();

        return new UsageSnapshot(
                tenant.getPlan(),
                tenantUsagePort.countForms(tenantId),
                tenantUsagePort.countResponsesThisMonth(tenantId, monthStart, monthEnd),
                userRepository.countByTenantIdAndActiveTrue(tenantId),
                tenantUsagePort.countConvocatorias(tenantId));
    }
}
