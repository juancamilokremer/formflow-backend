package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.security.JwtService;
import com.kodelabs.formflow.shared.web.ApiResponse;
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
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** backend#191 — POST /tenant/plan-upgrade-request. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RequestPlanUpgradeControllerTest {

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
    void tenantAdminRequestsAnUpgradeSuccessfully() {
        String token = tenantAdminToken();
        String payload = "{\"requestedPlan\":\"PRO\",\"message\":\"Necesitamos más convocatorias\"}";

        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/tenant/plan-upgrade-request", HttpMethod.POST,
                new HttpEntity<>(payload, jsonHeaders(token)), new org.springframework.core.ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    void anInvalidPlanLiteralIsRejected() {
        String token = tenantAdminToken();
        String payload = "{\"requestedPlan\":\"BOGUS\"}";

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/tenant/plan-upgrade-request", HttpMethod.POST,
                new HttpEntity<>(payload, jsonHeaders(token)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void aMissingPlanIsRejected() {
        String token = tenantAdminToken();
        String payload = "{\"message\":\"sin plan\"}";

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/tenant/plan-upgrade-request", HttpMethod.POST,
                new HttpEntity<>(payload, jsonHeaders(token)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void anUnauthenticatedRequestIsRejected() {
        // The default SimpleClientHttpRequestFactory (JDK HttpURLConnection) streams the POST
        // body and can't replay it once the server responds with anything but 2xx, so it
        // throws "cannot retry due to server authentication, in streaming mode" instead of
        // ever returning the 401 to this client — even with setOutputStreaming(false). Swap
        // to JdkClientHttpRequestFactory (java.net.http.HttpClient, Spring 6.1+), which doesn't
        // have this quirk — restored in `finally` since the TestRestTemplate bean (and its
        // RestTemplate) is shared across this class's tests.
        RestTemplate underlying = restTemplate.getRestTemplate();
        ClientHttpRequestFactory original = underlying.getRequestFactory();
        underlying.setRequestFactory(new JdkClientHttpRequestFactory());
        try {
            String payload = "{\"requestedPlan\":\"PRO\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.exchange(
                    "http://localhost:" + port + "/api/v1/tenant/plan-upgrade-request", HttpMethod.POST,
                    new HttpEntity<>(payload, headers), String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        } finally {
            underlying.setRequestFactory(original);
        }
    }

    private String tenantAdminToken() {
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .slug("plan-upgrade-test-" + UUID.randomUUID()).name("Empresa ABC").build());
        User user = userRepository.save(User.builder()
                .tenantId(tenant.getId()).email("admin-" + UUID.randomUUID() + "@abc.com").passwordHash("x")
                .firstName("Juan").lastName("Perez").role(UserRole.TENANT_ADMIN).build());
        return jwtService.generateAccessToken(user.getId(), tenant.getId(), user.getEmail(), "TENANT_ADMIN");
    }

    private HttpHeaders jsonHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
