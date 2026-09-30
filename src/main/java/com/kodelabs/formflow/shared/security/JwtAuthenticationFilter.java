package com.kodelabs.formflow.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.i18n.Messages;
import com.kodelabs.formflow.shared.tenant.TenantContext;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Filter that authenticates requests carrying an "Authorization: Bearer {jwt}" header.
 *
 * When the token is valid: populates the SecurityContext (principal = userId) and
 * the TenantContext (from the tenantId claim — trusted, signed source), unless the
 * tenant has since been suspended (backend#5) — in that case the request is rejected
 * with 403 right here, since a JWT issued before the suspension is otherwise still
 * valid for up to its 24h TTL and LoginService's check-at-login only covers new logins.
 * When invalid or absent: continues unauthenticated; protected routes will
 * respond 401 through the AuthenticationEntryPoint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final TenantRepositoryPort tenantRepository;
    private final ObjectMapper objectMapper;
    private final Messages messages;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader(AUTH_HEADER);
            if (header != null && header.startsWith(BEARER_PREFIX)) {
                var claims = jwtService.parseToken(header.substring(BEARER_PREFIX.length()));
                if (claims.isPresent() && !authenticate(claims.get(), request, response)) {
                    return;
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            // The request may never reach TenantFilter (e.g. rejected by the security
            // chain) — clearing here guarantees no leaks across pooled threads
            TenantContext.clear();
        }
    }

    /** Returns false when the request was rejected (response already written) and the
     *  filter chain must not continue. */
    private boolean authenticate(Claims claims, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String userId = claims.getSubject();
        String tenantId = claims.get(JwtService.CLAIM_TENANT_ID, String.class);
        String role = claims.get(JwtService.CLAIM_ROLE, String.class);

        Tenant tenant = tenantRepository.findById(UUID.fromString(tenantId)).orElse(null);
        if (tenant == null || !tenant.isActive()) {
            log.warn("Request rejected: tenant '{}' is {}", tenantId, tenant == null ? "unknown" : tenant.getStatus());
            writeForbidden(request, response);
            return false;
        }

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        TenantContext.setTenantId(tenantId);

        // Log correlation: every line of this request can be filtered by tenant/user.
        // Cleared by RequestLoggingFilter's MDC.clear() at the end of the request.
        MDC.put("tenantId", tenantId);
        MDC.put("userId", userId);
        return true;
    }

    private void writeForbidden(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.error(messages.getForLocale("error.tenant.suspended", localeFor(request)))));
    }

    /**
     * This filter runs before DispatcherServlet ever resolves the request locale, so
     * messages.get() (LocaleContextHolder-based) isn't usable here. HttpServletRequest#getLocale()
     * isn't a safe substitute either: without an Accept-Language header it falls back to the
     * JVM/OS default locale, which differs between machines/CI runners (this is exactly what
     * broke this filter's own tests — Spanish locally, English on the Linux CI runner) instead
     * of respecting I18nConfig's stated default of Spanish.
     */
    private Locale localeFor(HttpServletRequest request) {
        String header = request.getHeader("Accept-Language");
        return (header == null || header.isBlank()) ? Locale.forLanguageTag("es") : request.getLocale();
    }
}
