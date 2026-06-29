package com.kodelabs.formflow.shared.web;

import com.kodelabs.formflow.shared.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public final class ControllerUtils {

    private ControllerUtils() {}

    public static UUID tenantId() {
        return UUID.fromString(TenantContext.getTenantId());
    }

    public static UUID userId(Authentication auth) {
        return UUID.fromString((String) auth.getPrincipal());
    }

    /** Resolves the real client IP honoring X-Forwarded-For (reverse proxy). */
    public static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
