package com.kodelabs.formflow.shared.security;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantStatus;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards backend#5's acceptance criterion: suspending a tenant must reject every subsequent
 * request from its users with 403, not just new logins. LoginService already rejects a
 * suspended tenant at login time, but a JWT issued before the suspension (up to its 24h TTL)
 * would otherwise still authenticate fine — this is what JwtAuthenticationFilter's own
 * tenant-status check (added for #5) covers instead.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class JwtAuthenticationFilterTenantSuspensionTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TenantRepositoryPort tenantRepository;

    @Test
    void suspendingATenantRejectsItsAlreadyIssuedTokensWith403() {
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .slug("suspension-test-" + UUID.randomUUID())
                .name("Empresa de prueba")
                .status(TenantStatus.ACTIVE)
                .build());
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateAccessToken(userId, tenant.getId(), "admin@test.com", "TENANT_ADMIN");

        ResponseEntity<String> beforeSuspension = getTenant(token);
        assertThat(beforeSuspension.getStatusCode()).isEqualTo(HttpStatus.OK);

        tenant.setStatus(TenantStatus.SUSPENDED);
        tenantRepository.save(tenant);

        ResponseEntity<String> afterSuspension = getTenant(token);
        assertThat(afterSuspension.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(afterSuspension.getBody()).contains("suspendida");
    }

    private ResponseEntity<String> getTenant(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/tenant", HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
    }
}
