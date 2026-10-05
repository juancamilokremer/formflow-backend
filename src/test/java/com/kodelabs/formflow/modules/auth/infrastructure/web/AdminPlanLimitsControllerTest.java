package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.model.PlanLimits;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.out.PlanLimitsRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.PlanLimitsResponse;
import com.kodelabs.formflow.shared.security.JwtService;
import com.kodelabs.formflow.shared.web.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Round trip: SUPER_ADMIN edits a plan's limits, confirms both the admin GET and the
 *  public (unauthenticated) GET reflect the change immediately — the two must never drift. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminPlanLimitsControllerTest {

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
    private PlanLimitsRepositoryPort planLimitsRepository;

    @Test
    void superAdminUpdatesAPlanAndBothAdminAndPublicReadsReflectIt() {
        planLimitsRepository.save(TenantPlan.STARTER, new PlanLimits(10, 500, 3, 5, true));
        String token = superAdminToken();

        String payload = "{\"formsLimit\":25,\"responsesLimit\":null,\"usersLimit\":5,\"convocatoriasLimit\":8,\"canExportExcel\":true}";
        HttpHeaders headers = jsonHeaders(token);

        ResponseEntity<ApiResponse<PlanLimitsResponse>> putResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/admin/plan-limits/STARTER", HttpMethod.PUT,
                new HttpEntity<>(payload, headers), new ParameterizedTypeReference<>() {});

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        PlanLimitsResponse updated = putResponse.getBody().getData();
        assertThat(updated.formsLimit()).isEqualTo(25);
        assertThat(updated.responsesLimit()).isNull(); // null = unlimited, not the -1 sentinel
        assertThat(updated.usersLimit()).isEqualTo(5);
        assertThat(updated.convocatoriasLimit()).isEqualTo(8);

        ResponseEntity<ApiResponse<List<PlanLimitsResponse>>> adminList = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/admin/plan-limits", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), new ParameterizedTypeReference<>() {});
        assertThat(adminList.getBody().getData())
                .filteredOn(r -> r.plan() == TenantPlan.STARTER)
                .first().extracting(PlanLimitsResponse::formsLimit).isEqualTo(25);

        ResponseEntity<ApiResponse<List<PlanLimitsResponse>>> publicList = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/public/plan-limits", HttpMethod.GET,
                HttpEntity.EMPTY, new ParameterizedTypeReference<>() {});
        assertThat(publicList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(publicList.getBody().getData())
                .filteredOn(r -> r.plan() == TenantPlan.STARTER)
                .first().extracting(PlanLimitsResponse::formsLimit).isEqualTo(25);
    }

    @Test
    void negativeLimitOtherThanTheSentinelIsRejected() {
        String token = superAdminToken();
        String payload = "{\"formsLimit\":-5,\"responsesLimit\":null,\"usersLimit\":null,\"convocatoriasLimit\":null,\"canExportExcel\":true}";

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/admin/plan-limits/FREE", HttpMethod.PUT,
                new HttpEntity<>(payload, jsonHeaders(token)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void tenantAdminCannotEditPlanLimits() {
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .slug("plan-limits-test-" + UUID.randomUUID()).name("Empresa").build());
        User user = userRepository.save(User.builder()
                .tenantId(tenant.getId()).email("admin@test.com").passwordHash("x")
                .firstName("Admin").lastName("Test").role(UserRole.TENANT_ADMIN).build());
        String token = jwtService.generateAccessToken(user.getId(), tenant.getId(), "admin@test.com", "TENANT_ADMIN");

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/admin/plan-limits", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private String superAdminToken() {
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .slug("plan-limits-test-" + UUID.randomUUID()).name("Kode Labs").build());
        User user = userRepository.save(User.builder()
                .tenantId(tenant.getId()).email("root-" + UUID.randomUUID() + "@kodelabs.com").passwordHash("x")
                .firstName("Root").lastName("Admin").role(UserRole.SUPER_ADMIN).build());
        return jwtService.generateAccessToken(user.getId(), tenant.getId(), user.getEmail(), "SUPER_ADMIN");
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private HttpHeaders jsonHeaders(String token) {
        HttpHeaders headers = authHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
