package com.kodelabs.formflow.shared.security;

import com.kodelabs.formflow.shared.tenant.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter that authenticates requests carrying an "Authorization: Bearer {jwt}" header.
 *
 * When the token is valid: populates the SecurityContext (principal = userId) and
 * the TenantContext (from the tenantId claim — trusted, signed source).
 * When invalid or absent: continues unauthenticated; protected routes will
 * respond 401 through the AuthenticationEntryPoint.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader(AUTH_HEADER);
            if (header != null && header.startsWith(BEARER_PREFIX)) {
                jwtService.parseToken(header.substring(BEARER_PREFIX.length()))
                        .ifPresent(claims -> authenticate(claims, request));
            }
            filterChain.doFilter(request, response);
        } finally {
            // The request may never reach TenantFilter (e.g. rejected by the security
            // chain) — clearing here guarantees no leaks across pooled threads
            TenantContext.clear();
        }
    }

    private void authenticate(Claims claims, HttpServletRequest request) {
        String userId = claims.getSubject();
        String tenantId = claims.get(JwtService.CLAIM_TENANT_ID, String.class);
        String role = claims.get(JwtService.CLAIM_ROLE, String.class);

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        TenantContext.setTenantId(tenantId);
    }
}
