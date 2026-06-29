package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.SubmitCandidateResponseUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AnswerItem;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.SubmitCandidateResponseCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SubmitCandidateResponseResult;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.SubmitResponseRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.SubmitPublicResponseDto;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "Candidatos Públicos", description = "Endpoint público para que candidatos envíen su respuesta a una convocatoria. Sin autenticación requerida.")
public class PublicCandidateController {

    private final SubmitCandidateResponseUseCase submitCandidateResponse;

    @PostMapping("/{candidateToken}/responses")
    @Operation(
            summary = "Enviar respuesta de candidato",
            description = "Registra la respuesta de un candidato identificado por su token único. " +
                    "Calcula el puntaje automáticamente con los pesos de categorías de la convocatoria. " +
                    "Solo se puede responder una vez por candidato.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Respuesta registrada y puntaje calculado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Pregunta obligatoria sin respuesta o datos inválidos", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Token de candidato no encontrado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El candidato ya respondió o la convocatoria no está activa", content = @Content)
    public ResponseEntity<ApiResponse<SubmitPublicResponseDto>> submitResponse(
            @PathVariable UUID candidateToken,
            @Valid @RequestBody SubmitResponseRequest request) {

        List<AnswerItem> answers = request.answers().stream()
                .map(a -> new AnswerItem(a.questionId(), a.value()))
                .toList();

        SubmitCandidateResponseResult result = submitCandidateResponse.execute(
                new SubmitCandidateResponseCommand(candidateToken, request.startedAt(), answers));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new SubmitPublicResponseDto(result.respondentToken())));
    }
}
