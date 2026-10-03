package com.kodelabs.formflow.shared.web;

import com.kodelabs.formflow.shared.web.dto.response.PublicHealthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/** Full round trip, no auth header sent — confirms the endpoint is genuinely reachable by an
 *  external monitor (Upptime, see backend#28), not just unit-tested in isolation. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PublicHealthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void returnsOkStatusWithVersionAndTimestampWithoutAuthentication() {
        ResponseEntity<PublicHealthResponse> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/health/public", PublicHealthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("ok");
        assertThat(response.getBody().version()).isNotBlank();
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}
