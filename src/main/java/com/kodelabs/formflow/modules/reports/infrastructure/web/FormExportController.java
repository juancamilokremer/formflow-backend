package com.kodelabs.formflow.modules.reports.infrastructure.web;

import com.kodelabs.formflow.modules.reports.domain.model.ExportFormat;
import com.kodelabs.formflow.modules.reports.domain.port.in.ExportFormResponsesUseCase;
import com.kodelabs.formflow.modules.reports.domain.port.in.command.ExportFormResponsesQuery;
import com.kodelabs.formflow.modules.reports.domain.port.in.result.ExportResult;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.kodelabs.formflow.shared.web.ControllerUtils.tenantId;

@RestController
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
@Tag(name = "Exportación", description = "Exportar respuestas de un formulario. Requiere autenticación.")
@SecurityRequirement(name = "Bearer Auth")
public class FormExportController {

    private final ExportFormResponsesUseCase exportFormResponses;

    @GetMapping("/{formId}/export/excel")
    @Operation(
            summary = "Exportar respuestas a Excel",
            description = "Genera un archivo .xlsx con una fila por respuesta y una columna por pregunta, " +
                    "en el orden actual del formulario, con las respuestas de opción resueltas a texto legible.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Archivo Excel")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Formulario no encontrado", content = @Content)
    public ResponseEntity<byte[]> exportExcel(@PathVariable UUID formId) {
        return export(formId, ExportFormat.EXCEL);
    }

    @GetMapping("/{formId}/export/csv")
    @Operation(
            summary = "Exportar respuestas a CSV",
            description = "Misma estructura que el export a Excel, en formato CSV.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Archivo CSV")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Formulario no encontrado", content = @Content)
    public ResponseEntity<byte[]> exportCsv(@PathVariable UUID formId) {
        return export(formId, ExportFormat.CSV);
    }

    private ResponseEntity<byte[]> export(UUID formId, ExportFormat format) {
        ExportResult result = exportFormResponses.execute(new ExportFormResponsesQuery(formId, tenantId(), format));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.content());
    }
}
