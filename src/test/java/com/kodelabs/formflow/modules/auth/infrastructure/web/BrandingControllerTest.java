package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.BrandingResponse;
import com.kodelabs.formflow.shared.security.JwtService;
import com.kodelabs.formflow.shared.web.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Full round trip: upload a logo as TENANT_ADMIN, then confirm the public endpoint (no auth)
 *  reflects it immediately (cache invalidation) and serves the actual bytes back. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BrandingControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TenantRepositoryPort tenantRepository;

    @Test
    void uploadingALogoIsImmediatelyVisibleOnThePublicEndpoint() throws Exception {
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .slug("branding-test-" + UUID.randomUUID()).name("Empresa Branding").build());
        String token = jwtService.generateAccessToken(UUID.randomUUID(), tenant.getId(), "admin@test.com", "TENANT_ADMIN");

        byte[] png = pngOf(100, 50);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(png) {
            @Override public String getFilename() { return "logo.png"; }
        });
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(token);

        ResponseEntity<ApiResponse<BrandingResponse>> uploadResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/tenant/branding/logo", HttpMethod.POST,
                new HttpEntity<>(body, headers), new ParameterizedTypeReference<>() {});

        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String logoUrl = uploadResponse.getBody().getData().logoUrl();
        assertThat(logoUrl).contains("/api/v1/public/files/");

        ResponseEntity<ApiResponse<BrandingResponse>> publicResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/public/branding/" + tenant.getSlug(), HttpMethod.GET,
                HttpEntity.EMPTY, new ParameterizedTypeReference<>() {});

        assertThat(publicResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(publicResponse.getBody().getData().logoUrl()).isEqualTo(logoUrl);

        ResponseEntity<byte[]> downloadResponse = restTemplate.getForEntity(logoUrl, byte[].class);
        assertThat(downloadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void publicBrandingReturns404ForAnUnknownSlug() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/public/branding/no-existe-" + UUID.randomUUID(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void invalidHexColorReturns400() {
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .slug("branding-test-" + UUID.randomUUID()).name("Empresa").build());
        String token = jwtService.generateAccessToken(UUID.randomUUID(), tenant.getId(), "admin@test.com", "TENANT_ADMIN");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        String payload = "{\"name\":\"Empresa\",\"primaryColor\":\"rojo\",\"secondaryColor\":\"#222222\"}";

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/tenant/branding", HttpMethod.PUT,
                new HttpEntity<>(payload, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private byte[] pngOf(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
