package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.port.in.ActivateTenantUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.ChangeTenantPlanUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetAllPlanLimitsUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetGlobalStatsUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetTenantByIdUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.ListAllTenantsUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.SuspendTenantUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.UpdatePlanLimitsUseCase;
import com.kodelabs.formflow.modules.auth.domain.model.PlanLimits;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.model.TenantStatus;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ActivateTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ChangeTenantPlanCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetTenantByIdQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ListTenantsQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.SuspendTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdatePlanLimitsCommand;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request.ChangeTenantPlanRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request.UpdatePlanLimitsRequest;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.GlobalStatsResponse;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.PlanLimitsResponse;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.TenantPageResponse;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.TenantResponse;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Kode Labs' own super-administration over every tenant on the platform.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Admin", description = "Panel de super-administración de Kode Labs. Requiere rol SUPER_ADMIN.")
@SecurityRequirement(name = "Bearer Auth")
public class AdminController {

    private final ListAllTenantsUseCase listAllTenants;
    private final GetTenantByIdUseCase getTenantById;
    private final SuspendTenantUseCase suspendTenant;
    private final ActivateTenantUseCase activateTenant;
    private final ChangeTenantPlanUseCase changeTenantPlan;
    private final GetGlobalStatsUseCase getGlobalStats;
    private final GetAllPlanLimitsUseCase getAllPlanLimits;
    private final UpdatePlanLimitsUseCase updatePlanLimits;

    @GetMapping("/tenants")
    @Operation(summary = "Listar tenants", description = "Todos los tenants de la plataforma, paginados y con filtros opcionales.")
    public ResponseEntity<ApiResponse<TenantPageResponse>> listTenants(
            @RequestParam(defaultValue = "${app.pagination.default-page:0}") int page,
            @RequestParam(defaultValue = "${app.pagination.default-size:20}") int size,
            @RequestParam(required = false) TenantStatus status,
            @RequestParam(required = false) TenantPlan plan) {
        var result = listAllTenants.execute(new ListTenantsQuery(page, size, status, plan));
        return ResponseEntity.ok(ApiResponse.ok(TenantPageResponse.from(result)));
    }

    @GetMapping("/tenants/{id}")
    @Operation(summary = "Detalle de un tenant", description = "Sin scoping al tenant del llamador.")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenant(@PathVariable UUID id) {
        var result = getTenantById.execute(new GetTenantByIdQuery(id));
        return ResponseEntity.ok(ApiResponse.ok(TenantResponse.from(result)));
    }

    @PutMapping("/tenants/{id}/suspend")
    @Operation(summary = "Suspender tenant", description = "Sus usuarios reciben 403 en cada request a partir de ese momento.")
    public ResponseEntity<ApiResponse<TenantResponse>> suspend(@PathVariable UUID id) {
        var result = suspendTenant.execute(new SuspendTenantCommand(id));
        return ResponseEntity.ok(ApiResponse.ok(TenantResponse.from(result)));
    }

    @PutMapping("/tenants/{id}/activate")
    @Operation(summary = "Reactivar tenant")
    public ResponseEntity<ApiResponse<TenantResponse>> activate(@PathVariable UUID id) {
        var result = activateTenant.execute(new ActivateTenantCommand(id));
        return ResponseEntity.ok(ApiResponse.ok(TenantResponse.from(result)));
    }

    @PutMapping("/tenants/{id}/plan")
    @Operation(summary = "Cambiar el plan de un tenant")
    public ResponseEntity<ApiResponse<TenantResponse>> changePlan(
            @PathVariable UUID id, @Valid @RequestBody ChangeTenantPlanRequest request) {
        var result = changeTenantPlan.execute(new ChangeTenantPlanCommand(id, request.plan()));
        return ResponseEntity.ok(ApiResponse.ok(TenantResponse.from(result)));
    }

    @GetMapping("/stats")
    @Operation(summary = "Estadísticas globales de la plataforma")
    public ResponseEntity<ApiResponse<GlobalStatsResponse>> globalStats() {
        var result = getGlobalStats.execute();
        return ResponseEntity.ok(ApiResponse.ok(GlobalStatsResponse.from(result)));
    }

    @GetMapping("/plan-limits")
    @Operation(summary = "Límites de cada plan", description = "Los mismos 4 planes que alimenta la página pública de precios.")
    public ResponseEntity<ApiResponse<List<PlanLimitsResponse>>> getPlanLimits() {
        var result = getAllPlanLimits.execute().stream().map(PlanLimitsResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PutMapping("/plan-limits/{plan}")
    @Operation(summary = "Editar los límites de un plan", description = "Aplica de inmediato a todos los tenants en ese plan.")
    public ResponseEntity<ApiResponse<PlanLimitsResponse>> updatePlanLimits(
            @PathVariable TenantPlan plan, @Valid @RequestBody UpdatePlanLimitsRequest request) {
        var command = new UpdatePlanLimitsCommand(
                plan, unlimitedIfNull(request.formsLimit()), unlimitedIfNull(request.responsesLimit()),
                unlimitedIfNull(request.usersLimit()), unlimitedIfNull(request.convocatoriasLimit()),
                request.canExportExcel());
        var result = updatePlanLimits.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(PlanLimitsResponse.from(result)));
    }

    private static int unlimitedIfNull(Integer value) {
        return value == null ? PlanLimits.UNLIMITED : value;
    }
}
