package com.kodelabs.formflow.modules.auth.application.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Shared by GetPublicBrandingService (reads) and the three branding-mutating services
 * (which call invalidate() so a manual change is visible immediately instead of waiting out
 * the 10-min TTL) — same reasoning already applied to PlanLimitService in backend#6.
 * Caffeine directly, no Spring cache bean, keyed by tenant slug.
 */
@Component
public class BrandingCache {

    private final Cache<String, BrandingResult> cache;

    @Autowired
    public BrandingCache() {
        this(Ticker.systemTicker());
    }

    /** Package-private: lets tests advance a fake ticker to prove the TTL works. */
    BrandingCache(Ticker ticker) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(10))
                .ticker(ticker)
                .build();
    }

    public Optional<BrandingResult> get(String tenantSlug) {
        return Optional.ofNullable(cache.getIfPresent(tenantSlug));
    }

    public void put(String tenantSlug, BrandingResult result) {
        cache.put(tenantSlug, result);
    }

    public void invalidate(String tenantSlug) {
        cache.invalidate(tenantSlug);
    }
}
