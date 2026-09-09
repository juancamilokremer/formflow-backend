package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.ExportConvocatoriaRankingUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ExportConvocatoriaRankingQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.kodelabs.formflow.shared.web.ControllerUtils.tenantId;

@RestController
@RequestMapping("/api/v1/convocatorias/{id}/export")
@RequiredArgsConstructor
@Tag(name = "Exportación de convocatorias", description = "Exportar el ranking de candidatos de una convocatoria. Requiere autenticación.")
@SecurityRequirement(name = "Bearer Auth")
public class ConvocatoriaExportController {

    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ExportConvocatoriaRankingUseCase exportConvocatoriaRanking;

    @GetMapping("/excel")
    @Operation(
            summary = "Exportar el ranking de la convocatoria a Excel",
            description = "Genera un .xlsx con una fila por candidato: nombre, email, estado, rank, puntaje total, " +
                    "clasificación, puntaje por categoría y puntaje por formulario. Si se pasa candidateIds, solo " +
                    "exporta esos candidatos (conservando su rank dentro del ranking completo); sin ese parámetro " +
                    "exporta a todos.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Archivo Excel")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Convocatoria no encontrada", content = @Content)
    public ResponseEntity<byte[]> exportExcel(
            @PathVariable UUID id,
            @RequestParam(required = false) List<UUID> candidateIds) {
        var result = exportConvocatoriaRanking.execute(
                new ExportConvocatoriaRankingQuery(id, tenantId(), candidateIds));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .body(result.content());
    }
}
