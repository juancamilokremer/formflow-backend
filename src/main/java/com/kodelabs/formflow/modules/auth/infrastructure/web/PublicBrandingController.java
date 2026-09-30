package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.port.in.GetPublicBrandingUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetPublicBrandingQuery;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.BrandingResponse;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public — no auth, no X-Tenant-ID. Called on every public form load to apply the tenant's
 * visual branding, so the response is cached (BrandingCache, 10 min).
 */
@RestController
@RequestMapping("/api/v1/public/branding")
@RequiredArgsConstructor
@Tag(name = "Branding Público", description = "Branding para formularios públicos. Sin autenticación requerida.")
public class PublicBrandingController {

    private final GetPublicBrandingUseCase getPublicBranding;

    @GetMapping("/{tenantSlug}")
    @Operation(summary = "Branding de un tenant por slug", description = "Solo campos seguros de exponer públicamente.")
    public ResponseEntity<ApiResponse<BrandingResponse>> get(@PathVariable String tenantSlug) {
        var result = getPublicBranding.execute(new GetPublicBrandingQuery(tenantSlug));
        return ResponseEntity.ok(ApiResponse.ok(BrandingResponse.from(result)));
    }
}
