package com.kodelabs.formflow.shared.web;

import com.kodelabs.formflow.shared.tenant.TenantContext;
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
}
