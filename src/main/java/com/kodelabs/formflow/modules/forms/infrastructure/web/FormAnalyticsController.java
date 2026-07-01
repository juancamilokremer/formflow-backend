package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.GetFormScoringUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetFormStatsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormScoringQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormStatsQuery;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.FormScoringResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.FormStatsResponse;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.kodelabs.formflow.shared.web.ControllerUtils.tenantId;

@RestController
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
@Tag(name = "Formularios", description = "CRUD de formularios. Requiere autenticacion.")
@SecurityRequirement(name = "Bearer Auth")
public class FormAnalyticsController {

    private final GetFormScoringUseCase getFormScoring;
    private final GetFormStatsUseCase getFormStats;

    @GetMapping("/{id}/scoring-summary")
    @Operation(
            summary = "Resumen de scoring del formulario",
            description = "Retorna el puntaje máximo alcanzable por categoría para el formulario. " +
                    "Útil para configurar pesos en una convocatoria antes de activarla.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Resumen de scoring")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Formulario no encontrado o no pertenece al tenant", content = @Content)
    public ResponseEntity<ApiResponse<FormScoringResponse>> scoringSummary(@PathVariable UUID id) {
        var result = getFormScoring.execute(new GetFormScoringQuery(id, tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(FormScoringResponse.from(result)));
    }

    @GetMapping("/{id}/stats")
    @Operation(
            summary = "Estadísticas de respuestas del formulario",
            description = "Retorna la distribución de respuestas por pregunta. " +
                    "Incluye conteos y porcentajes para opciones (single/multiple/nps), " +
                    "promedio y mediana para escalas, y distribución por fila para matrices.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Estadísticas calculadas")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Formulario no encontrado o no pertenece al tenant", content = @Content)
    public ResponseEntity<ApiResponse<FormStatsResponse>> stats(@PathVariable UUID id) {
        var result = getFormStats.execute(new GetFormStatsQuery(id, tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(FormStatsResponse.from(result)));
    }
}
