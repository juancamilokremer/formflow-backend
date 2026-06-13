package com.kodelabs.formflow.shared.config;

import com.kodelabs.formflow.shared.tenant.TenantContext;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

/**
 * Async execution setup. ThreadLocal-based context (MDC, TenantContext) does
 * not flow to pool threads by itself — the TaskDecorator copies it from the
 * submitting thread so async logs keep requestId/tenantId correlation.
 *
 * Pool sizing is configurable via app.async.email.* properties.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${app.async.email.core-pool-size:2}")
    private int emailCorePoolSize;

    @Value("${app.async.email.max-pool-size:4}")
    private int emailMaxPoolSize;

    @Value("${app.async.email.queue-capacity:100}")
    private int emailQueueCapacity;

    @Bean(name = "emailExecutor")
    public ThreadPoolTaskExecutor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(emailCorePoolSize);
        executor.setMaxPoolSize(emailMaxPoolSize);
        executor.setQueueCapacity(emailQueueCapacity);
        executor.setThreadNamePrefix("email-");
        executor.setTaskDecorator(contextPropagatingDecorator());
        executor.initialize();
        return executor;
    }

    private TaskDecorator contextPropagatingDecorator() {
        return task -> {
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
            String tenantId = TenantContext.getTenantId();
            return () -> {
                try {
                    if (mdcContext != null) {
                        MDC.setContextMap(mdcContext);
                    }
                    if (tenantId != null) {
                        TenantContext.setTenantId(tenantId);
                    }
                    task.run();
                } finally {
                    MDC.clear();
                    TenantContext.clear();
                }
            };
        };
    }
}
