package com.kodelabs.formflow.shared.security;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
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
 * Guards backend#8's acceptance criterion: revoking a user's access must reject their
 * already-issued JWT with 401 on the very next request — not just at their next login.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class JwtAuthenticationFilterUserRevocationTest {

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
    void revokingAUserRejectsItsAlreadyIssuedTokenWith401() {
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .slug("revocation-test-" + UUID.randomUUID()).name("Empresa de prueba").build());
        User user = userRepository.save(User.builder()
                .tenantId(tenant.getId()).email("admin@test.com").passwordHash("x")
                .firstName("Admin").lastName("Test").role(UserRole.TENANT_ADMIN).active(true).build());
        String token = jwtService.generateAccessToken(user.getId(), tenant.getId(), "admin@test.com", "TENANT_ADMIN");

        ResponseEntity<String> beforeRevocation = getTenant(token);
        assertThat(beforeRevocation.getStatusCode()).isEqualTo(HttpStatus.OK);

        user.setActive(false);
        userRepository.save(user);

        ResponseEntity<String> afterRevocation = getTenant(token);
        assertThat(afterRevocation.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<String> getTenant(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/tenant", HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
    }
}
