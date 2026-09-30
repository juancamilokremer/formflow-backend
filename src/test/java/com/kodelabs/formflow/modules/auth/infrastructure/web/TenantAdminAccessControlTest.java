package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.security.JwtService;
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
 * Guards backend#5's role-scoping acceptance criteria: TENANT_ADMIN must never reach
 * /admin/**, and (symmetrically) a caller without TENANT_ADMIN must never reach /tenant/**.
 * @PreAuthorize("hasRole(...)") on each controller is what's expected to enforce this —
 * verified end-to-end here rather than just trusting the annotation is wired correctly.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TenantAdminAccessControlTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TenantRepositoryPort tenantRepository;

    @Autowired
    private UserRepositoryPort userRepository;

    @Test
    void tenantAdminCannotReachTheAdminPanel() {
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .slug("access-control-test-" + UUID.randomUUID()).name("Empresa").build());
        User user = userRepository.save(User.builder()
                .tenantId(tenant.getId()).email("admin@test.com").passwordHash("x")
                .firstName("Admin").lastName("Test").role(UserRole.TENANT_ADMIN).build());
        String token = jwtService.generateAccessToken(
                user.getId(), tenant.getId(), "admin@test.com", "TENANT_ADMIN");

        ResponseEntity<String> response = exchange("/api/v1/admin/tenants", token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void superAdminCannotReachTenantSelfService() {
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .slug("access-control-test-" + UUID.randomUUID()).name("Empresa").build());
        User user = userRepository.save(User.builder()
                .tenantId(tenant.getId()).email("root@kodelabs.com").passwordHash("x")
                .firstName("Root").lastName("Admin").role(UserRole.SUPER_ADMIN).build());
        String token = jwtService.generateAccessToken(
                user.getId(), tenant.getId(), "root@kodelabs.com", "SUPER_ADMIN");

        ResponseEntity<String> response = exchange("/api/v1/tenant", token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private ResponseEntity<String> exchange(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return restTemplate.exchange(
                "http://localhost:" + port + path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }
}
