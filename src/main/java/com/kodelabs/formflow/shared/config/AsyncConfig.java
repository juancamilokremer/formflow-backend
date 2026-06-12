package com.kodelabs.formflow.shared.config;

import com.kodelabs.formflow.shared.tenant.TenantContext;
import org.slf4j.MDC;
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
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "emailExecutor")
    public ThreadPoolTaskExecutor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
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
