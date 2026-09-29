package com.kodelabs.formflow.shared.config;

import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

/**
 * bucket4j-spring-boot-starter 0.10.x only wires its rate-limit bucket cache through a
 * javax.cache.CacheManager (JCache/JSR-107) — plain Spring Cache (spring.cache.type=caffeine)
 * satisfies a different interface, so the rate limiter's servlet filter never activated despite
 * a config that otherwise looked complete. See backend#165.
 */
@Configuration
public class RateLimitCacheConfig {

    private static final String CACHE_NAME = "rate-limit-buckets";

    @Bean
    public CacheManager rateLimitCacheManager() {
        CacheManager cacheManager = Caching
                .getCachingProvider(CaffeineCachingProvider.class.getName())
                .getCacheManager();

        // The JCache CacheManager returned above is a singleton keyed by URI/classloader, not
        // scoped to this Spring context — multiple ApplicationContexts in the same JVM (e.g.
        // separate MOCK vs RANDOM_PORT test contexts) share it, so re-creating an existing
        // cache throws instead of being a no-op.
        if (cacheManager.getCache(CACHE_NAME) == null) {
            CaffeineConfiguration<String, Object> configuration = new CaffeineConfiguration<>();
            configuration.setMaximumSize(OptionalLong.of(100_000));
            configuration.setExpireAfterAccess(OptionalLong.of(TimeUnit.HOURS.toNanos(1)));
            cacheManager.createCache(CACHE_NAME, configuration);
        }
        return cacheManager;
    }
}
