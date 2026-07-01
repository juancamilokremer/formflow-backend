package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.GetResponseDetailUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetResponsesUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetResponseDetailQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetResponsesQuery;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.ResponseDetailResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.ResponsePageResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.kodelabs.formflow.shared.web.ControllerUtils.tenantId;

@RestController
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
@Tag(name = "Respuestas", description = "Consulta de respuestas de un formulario. Requiere autenticación.")
@SecurityRequirement(name = "Bearer Auth")
public class FormResponseController {

    private final GetResponsesUseCase getResponses;
    private final GetResponseDetailUseCase getResponseDetail;

    @GetMapping("/{formId}/responses")
    @Operation(
            summary = "Listar respuestas",
            description = "Retorna respuestas de un formulario paginadas, ordenadas por fecha de envío descendente. " +
                    "Incluye puntaje total para respuestas de convocatoria.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Página de respuestas")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Formulario no encontrado", content = @Content)
    public ResponseEntity<ApiResponse<ResponsePageResponse>> getResponses(
            @PathVariable UUID formId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = getResponses.execute(new GetResponsesQuery(formId, tenantId(), page, size));
        return ResponseEntity.ok(ApiResponse.ok(ResponsePageResponse.from(result)));
    }

    @GetMapping("/{formId}/responses/{responseId}")
    @Operation(
            summary = "Detalle de respuesta",
            description = "Retorna la respuesta completa con todas las respuestas pregunta a pregunta " +
                    "y el snapshot del formulario al momento de responder.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Detalle de la respuesta")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Respuesta o formulario no encontrado", content = @Content)
    public ResponseEntity<ApiResponse<ResponseDetailResponse>> getResponseDetail(
            @PathVariable UUID formId,
            @PathVariable UUID responseId) {
        var result = getResponseDetail.execute(new GetResponseDetailQuery(formId, responseId, tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(ResponseDetailResponse.from(result)));
    }
}
