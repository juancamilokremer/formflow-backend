package com.kodelabs.formflow.shared.web;

import com.kodelabs.formflow.shared.web.dto.response.PublicHealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Lightweight, unauthenticated status payload for external uptime monitors (Upptime,
 * see backend#28) — deliberately separate from /actuator/health, which requires
 * authorization to show details and isn't meant for public polling.
 */
@RestController
@Tag(name = "Health Pública", description = "Status check sin autenticación, para monitoreo externo de uptime.")
public class PublicHealthController {

    // Optional: BuildProperties only exists when META-INF/build-info.properties was generated
    // (mvn package / a real deploy) — absent during plain `mvn spring-boot:run` in local dev.
    private final Optional<BuildProperties> buildProperties;

    public PublicHealthController(Optional<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/api/v1/health/public")
    @Operation(summary = "Status check público", description = "Sin autenticación. Usado por el monitor de uptime externo.")
    public ResponseEntity<PublicHealthResponse> publicHealth() {
        String version = buildProperties.map(BuildProperties::getVersion).orElse("dev");
        return ResponseEntity.ok(PublicHealthResponse.up(version));
    }
}
