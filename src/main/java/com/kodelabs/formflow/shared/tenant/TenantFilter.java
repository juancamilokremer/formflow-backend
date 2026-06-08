package com.kodelabs.formflow.shared.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que extrae el tenantId de cada request y lo almacena en TenantContext.
 *
 * Estrategia: lee el header X-Tenant-ID.
 * En el futuro se puede extender para resolución por subdominio.
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
            if (tenantId != null && !tenantId.isBlank()) {
                TenantContext.setTenantId(tenantId.trim());
            }
            filterChain.doFilter(request, response);
        } finally {
            // Siempre limpiar para evitar leaks entre requests en el thread pool
            TenantContext.clear();
        }
    }
}
