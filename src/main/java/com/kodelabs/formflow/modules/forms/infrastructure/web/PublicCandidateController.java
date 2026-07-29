package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.GetCandidatePortalUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetPublicCandidateFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.SubmitCandidateResponseUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AnswerItem;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetCandidatePortalQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetPublicCandidateFormQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.SubmitCandidateResponseCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidatePortalResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicCandidateFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SubmitCandidateResponseResult;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.SubmitResponseRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.CandidatePortalResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.PublicCandidateFormResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.SubmitPublicResponseDto;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidatos Públicos", description = "Endpoint público para que candidatos vean y respondan los formularios de su convocatoria. Sin autenticación requerida.")
public class PublicCandidateController {

    private final GetCandidatePortalUseCase getCandidatePortal;
    private final GetPublicCandidateFormUseCase getPublicCandidateForm;
    private final SubmitCandidateResponseUseCase submitCandidateResponse;

    @GetMapping("/{candidateToken}")
    @Operation(
            summary = "Obtener el portal del candidato",
            description = "Retorna los datos de la convocatoria y la lista de formularios asignados al candidato " +
                    "con su estado (pendiente/completado). No requiere autenticación.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Portal del candidato")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Token de candidato no encontrado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "La convocatoria ya está cerrada", content = @Content)
    public ResponseEntity<ApiResponse<CandidatePortalResponse>> getPortal(
            @PathVariable UUID candidateToken) {
        CandidatePortalResult result = getCandidatePortal.execute(new GetCandidatePortalQuery(candidateToken));
        return ResponseEntity.ok(ApiResponse.ok(CandidatePortalResponse.from(result)));
    }

    @GetMapping("/{candidateToken}/forms/{formId}")
    @Operation(
            summary = "Obtener un formulario puntual del candidato",
            description = "Retorna la estructura de uno de los formularios de la convocatoria, " +
                    "junto con datos de la convocatoria y si ese formulario específico ya fue respondido. " +
                    "No requiere autenticación.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Formulario listo para responder")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "El formulario no pertenece a la convocatoria del candidato", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Token de candidato no encontrado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "La convocatoria ya está cerrada", content = @Content)
    public ResponseEntity<ApiResponse<PublicCandidateFormResponse>> getCandidateForm(
            @PathVariable UUID candidateToken,
            @PathVariable UUID formId) {
        PublicCandidateFormResult result = getPublicCandidateForm.execute(
                new GetPublicCandidateFormQuery(candidateToken, formId));
        return ResponseEntity.ok(ApiResponse.ok(PublicCandidateFormResponse.from(result)));
    }

    @PostMapping("/{candidateToken}/forms/{formId}/responses")
    @Operation(
            summary = "Enviar respuesta de candidato a un formulario",
            description = "Registra la respuesta de un candidato a uno de los formularios de la convocatoria. " +
                    "Calcula el puntaje de ese formulario automáticamente con sus pesos de categorías. " +
                    "Solo se puede responder una vez cada formulario; el candidato puede responder los demás " +
                    "formularios de la convocatoria por separado.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Respuesta registrada y puntaje calculado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Pregunta obligatoria sin respuesta, formulario no pertenece a la convocatoria, o datos inválidos", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Token de candidato no encontrado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Ese formulario ya fue respondido o la convocatoria no está activa", content = @Content)
    public ResponseEntity<ApiResponse<SubmitPublicResponseDto>> submitResponse(
            @PathVariable UUID candidateToken,
            @PathVariable UUID formId,
            @Valid @RequestBody SubmitResponseRequest request) {

        List<AnswerItem> answers = request.answers().stream()
                .map(a -> new AnswerItem(a.questionId(), a.value()))
                .toList();

        SubmitCandidateResponseResult result = submitCandidateResponse.execute(
                new SubmitCandidateResponseCommand(candidateToken, formId, request.startedAt(), answers));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new SubmitPublicResponseDto(result.respondentToken())));
    }
}
