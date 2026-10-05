package com.kodelabs.formflow.modules.auth.infrastructure.web;

import com.kodelabs.formflow.modules.auth.domain.port.in.GetAllPlanLimitsUseCase;
import com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response.PlanLimitsResponse;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public — no auth, no X-Tenant-ID. Feeds the public pricing page (landing + /plans)
 * with the exact same numbers an admin edits in AdminController, so the two can never
 * drift apart.
 */
@RestController
@RequestMapping("/api/v1/public/plan-limits")
@RequiredArgsConstructor
@Tag(name = "Límites de Plan (Público)", description = "Límites reales de cada plan, para la página de precios. Sin autenticación requerida.")
public class PublicPlanLimitsController {

    private final GetAllPlanLimitsUseCase getAllPlanLimits;

    @GetMapping
    @Operation(summary = "Límites de cada plan")
    public ResponseEntity<ApiResponse<List<PlanLimitsResponse>>> get() {
        var result = getAllPlanLimits.execute().stream().map(PlanLimitsResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
