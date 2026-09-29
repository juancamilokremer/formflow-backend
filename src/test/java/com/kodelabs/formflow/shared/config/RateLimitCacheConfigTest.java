package com.kodelabs.formflow.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards backend#165. bucket4j-spring-boot-starter 0.10.x wires its rate-limit filter through
 * a javax.cache.CacheManager (JCache), not Spring's own cache abstraction — with plain
 * spring.cache.type=caffeine the filter's @ConditionalOnBean(SyncCacheResolver.class) never
 * matched, so the whole servlet filter silently never registered. No error, no log, just an
 * endpoint that accepted unlimited requests despite a config that looked complete.
 *
 * Needs a real embedded server (RANDOM_PORT + TestRestTemplate, not MockMvc): the filter is
 * added via WebServerFactoryCustomizer at the Tomcat level, which MockMvc's dispatch never
 * goes through. The URL match is a raw path pattern, so a form that doesn't exist is enough —
 * the limiter runs before the request ever reaches the controller.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RateLimitCacheConfigTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    // Single test: the rate-limit bucket is keyed per client IP, shared across every request
    // this JVM makes to this endpoint for the life of the cache — a second @Test method would
    // start with an already-exhausted bucket instead of a clean one.
    @Test
    void publicResponseSubmissionIsRateLimitedAfterTenRequestsPerMinute() {
        String url = "http://localhost:" + port + "/api/v1/public/forms/" + UUID.randomUUID() + "/responses";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"answers\":[]}", headers);

        int rateLimited = 0;
        ResponseEntity<String> lastRejection = null;
        for (int i = 0; i < 13; i++) {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode().value() == 429) {
                rateLimited++;
                lastRejection = response;
            }
        }

        assertThat(rateLimited)
                .as("requests beyond the 10/min cap must be rejected with 429")
                .isEqualTo(3);
        assertThat(lastRejection.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(lastRejection.getBody())
                .contains("Has enviado demasiadas respuestas")
                .doesNotContain("errorId");
    }
}
