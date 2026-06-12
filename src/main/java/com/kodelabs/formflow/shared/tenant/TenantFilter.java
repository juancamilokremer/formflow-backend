package com.kodelabs.formflow.shared.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that extracts the tenantId from each request and stores it in TenantContext.
 *
 * Strategy: reads the X-Tenant-ID header.
 * Can be extended in the future to resolve the tenant by subdomain.
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String tenantId = request.getHeader(TENANT_HEADER);
            // The header only applies when the JWT did not already set the tenant:
            // the signed token claim is the trusted source and a client must not
            // be able to spoof it by sending another tenant's X-Tenant-ID
            if (tenantId != null && !tenantId.isBlank() && !TenantContext.hasTenant()) {
                TenantContext.setTenantId(tenantId.trim());
            }
            filterChain.doFilter(request, response);
        } finally {
            // Always clear to avoid leaks across pooled threads
            TenantContext.clear();
        }
    }
}
