package com.kodelabs.formflow.modules.auth.application.service;

import com.github.benmanes.caffeine.cache.Ticker;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class BrandingCacheTest {

    private final AtomicLong nanos = new AtomicLong();
    private final Ticker testTicker = nanos::get;
    private final BrandingCache cache = new BrandingCache(testTicker);

    private final BrandingResult result = new BrandingResult("Empresa", null, null, null, null);

    @Test
    void returnsEmptyWhenNothingCached() {
        assertThat(cache.get("empresa-abc")).isEmpty();
    }

    @Test
    void returnsTheCachedValueWithinTheTtl() {
        cache.put("empresa-abc", result);

        assertThat(cache.get("empresa-abc")).contains(result);
    }

    @Test
    void expiresAfterTenMinutes() {
        cache.put("empresa-abc", result);
        nanos.addAndGet(Duration.ofMinutes(11).toNanos());

        assertThat(cache.get("empresa-abc")).isEmpty();
    }

    @Test
    void invalidateForcesAMiss() {
        cache.put("empresa-abc", result);
        cache.invalidate("empresa-abc");

        assertThat(cache.get("empresa-abc")).isEmpty();
    }
}
