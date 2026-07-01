package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.GetConvocatoriaStatsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetRankingUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetConvocatoriaStatsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetRankingQuery;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.ConvocatoriaStatsResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.RankingEntryResponse;
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

import java.util.List;
import java.util.UUID;

import static com.kodelabs.formflow.shared.web.ControllerUtils.tenantId;

@RestController
@RequestMapping("/api/v1/convocatorias")
@RequiredArgsConstructor
@Tag(name = "Convocatorias", description = "CRUD y ciclo de vida de convocatorias. Requiere autenticación.")
@SecurityRequirement(name = "Bearer Auth")
public class ConvocatoriaAnalyticsController {

    private final GetRankingUseCase getRanking;
    private final GetConvocatoriaStatsUseCase getConvocatoriaStats;

    @GetMapping("/{id}/ranking")
    @Operation(
            summary = "Ranking de candidatos",
            description = "Retorna todos los candidatos ordenados por puntaje total DESC. " +
                    "Incluye rank numérico, clasificación (APTO/REVISAR/NO_APTO) y puntaje por categoría. " +
                    "Candidatos sin respuesta aparecen al final con rank nulo.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Ranking calculado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Convocatoria no encontrada", content = @Content)
    public ResponseEntity<ApiResponse<List<RankingEntryResponse>>> ranking(@PathVariable UUID id) {
        var results = getRanking.execute(new GetRankingQuery(id, tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(
                results.stream().map(RankingEntryResponse::from).toList()));
    }

    @GetMapping("/{id}/stats")
    @Operation(
            summary = "Estadísticas de la convocatoria",
            description = "Retorna agregados: total candidatos, respondidos, pendientes, " +
                    "distribución APTO/REVISAR/NO_APTO y porcentaje de participación.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Estadísticas calculadas")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Convocatoria no encontrada", content = @Content)
    public ResponseEntity<ApiResponse<ConvocatoriaStatsResponse>> stats(@PathVariable UUID id) {
        var result = getConvocatoriaStats.execute(new GetConvocatoriaStatsQuery(id, tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(ConvocatoriaStatsResponse.from(result)));
    }
}
