package com.kodelabs.formflow.shared.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring Cache support. CaffeineCacheManager is auto-configured
 * from spring.cache.* properties in application.yml.
 */
@Configuration
@EnableCaching
public class CacheConfig {}
