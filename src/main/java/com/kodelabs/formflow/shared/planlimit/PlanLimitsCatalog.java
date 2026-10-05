package com.kodelabs.formflow.shared.planlimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.kodelabs.formflow.modules.auth.domain.model.PlanLimits;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.port.out.PlanLimitsRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Cached lookup of admin-editable plan limits (4 rows, rarely change). Backs
 * PlanLimitService's enforcement checks and GetTenantUsageService — both need
 * the DB-backed value, not the hardcoded switch that used to live on PlanLimits
 * itself. UpdatePlanLimitsService calls invalidateAll() for immediate effect
 * instead of waiting out the TTL, same spirit as PlanLimitService.invalidate().
 */
@Component
public class PlanLimitsCatalog {

    private final PlanLimitsRepositoryPort repository;
    private final Cache<TenantPlan, PlanLimits> cache;

    @Autowired
    public PlanLimitsCatalog(PlanLimitsRepositoryPort repository) {
        this(repository, Ticker.systemTicker());
    }

    /** Package-private: lets tests advance a fake ticker to prove the TTL works. */
    PlanLimitsCatalog(PlanLimitsRepositoryPort repository, Ticker ticker) {
        this.repository = repository;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .ticker(ticker)
                .build();
    }

    public PlanLimits forPlan(TenantPlan plan) {
        return cache.get(plan, repository::findByPlan);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }
}
