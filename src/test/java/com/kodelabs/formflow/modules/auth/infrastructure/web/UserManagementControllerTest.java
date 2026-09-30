package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserInvitationRepositoryPort;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end through real HTTP + H2: an EDITOR must never reach the user-management
 * endpoints (backend#8's own acceptance criterion), and accepting a real invitation must
 * let the new account log in right away.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserManagementControllerTest {

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

    @Autowired
    private UserInvitationRepositoryPort invitationRepository;

    @Autowired
    private TokenServicePort tokenService;

    @Test
    void anEditorCannotReachUserManagementEndpoints() {
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .slug("usermgmt-test-" + UUID.randomUUID()).name("Empresa").build());
        User editor = userRepository.save(User.builder()
                .tenantId(tenant.getId()).email("editor@test.com").passwordHash("x")
                .firstName("Ed").lastName("Itor").role(UserRole.EDITOR).active(true).build());
        String token = jwtService.generateAccessToken(editor.getId(), tenant.getId(), "editor@test.com", "EDITOR");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/users", HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void acceptingARealInvitationLetsTheNewAccountLogInImmediately() {
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .slug("usermgmt-accept-" + UUID.randomUUID()).name("Empresa Invitaciones").build());
        User admin = userRepository.save(User.builder()
                .tenantId(tenant.getId()).email("admin@test.com").passwordHash("x")
                .firstName("Ad").lastName("Min").role(UserRole.TENANT_ADMIN).active(true).build());

        String rawToken = tokenService.generateOpaqueToken();
        invitationRepository.save(UserInvitation.builder()
                .tenantId(tenant.getId()).email("invitado@test.com").role(UserRole.EDITOR)
                .tokenHash(tokenService.hashToken(rawToken)).invitedByUserId(admin.getId())
                .expiresAt(Instant.now().plusSeconds(3600)).build());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String acceptPayload = "{\"firstName\":\"Nuevo\",\"lastName\":\"Usuario\",\"password\":\"password123\"}";

        ResponseEntity<String> acceptResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/public/invitations/" + rawToken + "/accept",
                HttpMethod.POST, new HttpEntity<>(acceptPayload, headers), String.class);
        assertThat(acceptResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        String loginPayload = "{\"tenantSlug\":\"" + tenant.getSlug()
                + "\",\"email\":\"invitado@test.com\",\"password\":\"password123\"}";
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/auth/login", new HttpEntity<>(loginPayload, headers), String.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).contains("\"role\":\"EDITOR\"");
    }
}
