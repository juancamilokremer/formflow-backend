package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.AddQuestionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.DeleteQuestionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.ReorderQuestionsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateQuestionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddQuestionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteQuestionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ReorderQuestionsCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateQuestionCommand;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.AddQuestionRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.QuestionResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.ReorderQuestionsRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.UpdateQuestionRequest;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.kodelabs.formflow.shared.web.ControllerUtils.tenantId;
import static com.kodelabs.formflow.shared.web.ControllerUtils.userId;

@RestController
@RequestMapping("/api/v1/forms/{formId}/sections/{sectionId}/questions")
@RequiredArgsConstructor
@Tag(name = "Preguntas", description = "CRUD de preguntas dentro de una seccion. Requiere autenticacion.")
@SecurityRequirement(name = "Bearer Auth")
public class FormQuestionController {

    private final AddQuestionUseCase addQuestion;
    private final UpdateQuestionUseCase updateQuestion;
    private final DeleteQuestionUseCase deleteQuestion;
    private final ReorderQuestionsUseCase reorderQuestions;

    @PostMapping
    @Operation(
            summary = "Agregar una pregunta a la seccion",
            description = "La pregunta se agrega al final de la seccion. El campo 'config' varia segun el 'type'. " +
                    "Para SCALE con scoringType=AUTO los puntajes se calculan automaticamente. " +
                    "Incrementa la version del formulario.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Pregunta creada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos o config invalidos", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Seccion o formulario no encontrado", content = @Content)
    public ResponseEntity<ApiResponse<QuestionResponse>> add(
            @PathVariable UUID formId, @PathVariable UUID sectionId,
            @Valid @RequestBody AddQuestionRequest request, Authentication auth) {
        var result = addQuestion.execute(new AddQuestionCommand(
                formId, sectionId, tenantId(), userId(auth),
                request.title(), request.description(), request.type(),
                request.required(), request.categoryId(), request.timeLimitSeconds(),
                request.config()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(QuestionResponse.from(result)));
    }

    @PutMapping("/{questionId}")
    @Operation(
            summary = "Actualizar una pregunta",
            description = "Actualiza todos los campos de la pregunta incluyendo el config. " +
                    "Para SCALE con scoringType=AUTO recalcula los puntajes automaticamente.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pregunta actualizada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos o config invalidos", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pregunta no encontrada", content = @Content)
    public ResponseEntity<ApiResponse<QuestionResponse>> update(
            @PathVariable UUID formId, @PathVariable UUID sectionId,
            @PathVariable UUID questionId,
            @Valid @RequestBody UpdateQuestionRequest request, Authentication auth) {
        var result = updateQuestion.execute(new UpdateQuestionCommand(
                questionId, sectionId, formId, tenantId(), userId(auth),
                request.title(), request.description(), request.type(),
                request.required(), request.categoryId(), request.timeLimitSeconds(),
                request.config()));
        return ResponseEntity.ok(ApiResponse.ok(QuestionResponse.from(result)));
    }

    @DeleteMapping("/{questionId}")
    @Operation(
            summary = "Eliminar una pregunta (soft delete)",
            description = "Marca la pregunta como eliminada sin afectar respuestas historicas. " +
                    "Incrementa la version del formulario.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pregunta eliminada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pregunta no encontrada", content = @Content)
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID formId, @PathVariable UUID sectionId,
            @PathVariable UUID questionId, Authentication auth) {
        deleteQuestion.execute(new DeleteQuestionCommand(questionId, sectionId, formId, tenantId(), userId(auth)));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/reorder")
    @Operation(
            summary = "Reordenar preguntas de la seccion",
            description = "Recibe la lista completa de IDs de preguntas activas en el nuevo orden. " +
                    "Debe incluir exactamente los mismos IDs que las preguntas activas actuales. " +
                    "Incrementa la version del formulario.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Preguntas reordenadas")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Los IDs no coinciden con las preguntas activas", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Formulario no encontrado", content = @Content)
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> reorder(
            @PathVariable UUID formId, @PathVariable UUID sectionId,
            @Valid @RequestBody ReorderQuestionsRequest request, Authentication auth) {
        var results = reorderQuestions.execute(new ReorderQuestionsCommand(
                sectionId, formId, tenantId(), userId(auth), request.orderedQuestionIds()));
        return ResponseEntity.ok(ApiResponse.ok(
                results.stream().map(QuestionResponse::from).toList()));
    }
}
