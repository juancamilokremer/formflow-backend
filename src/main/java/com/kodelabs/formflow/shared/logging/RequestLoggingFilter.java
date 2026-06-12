package com.kodelabs.formflow.shared.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * First filter in the chain. Provides log correlation and the access log:
 *
 * - Generates a short requestId per request, stores it in the MDC (every log
 *   line of the request carries it) and returns it as the X-Request-Id header
 *   so clients can report it to support.
 * - Logs method, path, status and duration: DEBUG for 2xx/3xx, INFO for 4xx,
 *   WARN for 5xx — in prod (root INFO) only problematic requests show up.
 * - Clears the MDC at the end to avoid leaks across pooled threads.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(MDC_REQUEST_ID, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            logAccess(request, response, System.currentTimeMillis() - startTime);
            MDC.clear();
        }
    }

    private void logAccess(HttpServletRequest request, HttpServletResponse response, long durationMs) {
        int status = response.getStatus();
        String line = "{} {} -> {} ({} ms)";
        Object[] args = {request.getMethod(), request.getRequestURI(), status, durationMs};

        if (status >= 500) {
            log.warn(line, args);
        } else if (status >= 400) {
            log.info(line, args);
        } else {
            log.debug(line, args);
        }
    }
}
