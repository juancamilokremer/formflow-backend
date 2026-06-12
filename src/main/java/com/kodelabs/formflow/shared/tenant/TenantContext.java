package com.kodelabs.formflow.shared.tenant;

/**
 * Holds the tenantId of the current request using a ThreadLocal.
 * Set by JwtAuthenticationFilter (from the signed token claim) or by
 * TenantFilter (from the X-Tenant-ID header) and cleared when the request ends.
 */
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }
}
