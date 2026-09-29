package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.port.in.GetTenantUsageUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetTenantUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.UpdateTenantUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetTenantQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetTenantUsageQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdateTenantCommand;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request.UpdateTenantRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.TenantResponse;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.TenantUsageResponse;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.kodelabs.formflow.shared.web.ControllerUtils.tenantId;

/**
 * Self-service for the tenant admin over its own tenant. Never reads a tenant id
 * from a path/query param — always from the JWT via ControllerUtils.tenantId().
 */
@RestController
@RequestMapping("/api/v1/tenant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Tag(name = "Tenant", description = "Self-service de la empresa del usuario autenticado. Requiere rol TENANT_ADMIN.")
@SecurityRequirement(name = "Bearer Auth")
public class TenantController {

    private final GetTenantUseCase getTenant;
    private final UpdateTenantUseCase updateTenant;
    private final GetTenantUsageUseCase getTenantUsage;

    @GetMapping
    @Operation(summary = "Obtener el tenant actual", description = "Información de la empresa del usuario autenticado.")
    public ResponseEntity<ApiResponse<TenantResponse>> get() {
        var result = getTenant.execute(new GetTenantQuery(tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(TenantResponse.from(result)));
    }

    @PutMapping
    @Operation(summary = "Actualizar el tenant actual", description = "El slug es inmutable — enviarlo distinto al actual retorna 400.")
    public ResponseEntity<ApiResponse<TenantResponse>> update(@Valid @RequestBody UpdateTenantRequest request) {
        var result = updateTenant.execute(new UpdateTenantCommand(
                tenantId(), request.name(), request.logoUrl(),
                request.primaryColor(), request.secondaryColor(), request.slug()));
        return ResponseEntity.ok(ApiResponse.ok(TenantResponse.from(result)));
    }

    @GetMapping("/usage")
    @Operation(summary = "Uso actual del tenant", description = "Forms creados, respuestas del mes y usuarios, con los límites del plan.")
    public ResponseEntity<ApiResponse<TenantUsageResponse>> usage() {
        var result = getTenantUsage.execute(new GetTenantUsageQuery(tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(TenantUsageResponse.from(result)));
    }
}
