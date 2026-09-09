package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.ExportCandidateResponsePdfUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ExportCandidateResponsePdfQuery;
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
@RequestMapping("/api/v1/convocatorias/{convocatoriaId}/candidates/{candidateId}/export")
@RequiredArgsConstructor
@Tag(name = "Exportación de candidatos", description = "Exportar la respuesta de un candidato dentro de una convocatoria. Requiere autenticación.")
@SecurityRequirement(name = "Bearer Auth")
public class CandidateExportController {

    private final ExportCandidateResponsePdfUseCase exportCandidateResponsePdf;

    @GetMapping("/pdf")
    @Operation(
            summary = "Exportar la respuesta de un candidato a PDF",
            description = "Genera un PDF con los datos del candidato, el puntaje total y, por cada formulario " +
                    "que haya respondido dentro de la convocatoria, el puntaje por categoría y sus respuestas " +
                    "pregunta a pregunta. Usa el form_snapshot de cada respuesta — nunca la versión actual del formulario.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Archivo PDF")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Convocatoria o candidato no encontrado", content = @Content)
    public ResponseEntity<byte[]> exportPdf(
            @PathVariable UUID convocatoriaId,
            @PathVariable UUID candidateId) {
        var result = exportCandidateResponsePdf.execute(
                new ExportCandidateResponsePdfQuery(convocatoriaId, candidateId, tenantId()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(result.content());
    }
}
