package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.GetPublicFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.SubmitPublicResponseUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AnswerItem;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetPublicFormQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.SubmitPublicResponseCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SubmitPublicResponseResult;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.SubmitResponseRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.PublicFormResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.SubmitPublicResponseDto;
import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.ratelimit.RateLimitService;
import com.kodelabs.formflow.shared.web.ApiResponse;
import com.kodelabs.formflow.shared.web.ControllerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Formularios Públicos", description = "Endpoints públicos para respondentes. Sin autenticación requerida.")
public class PublicFormController {

    private final GetPublicFormUseCase getPublicForm;
    private final SubmitPublicResponseUseCase submitPublicResponse;
    private final RateLimitService rateLimitService;

    @GetMapping("/forms/{formId}")
    @Operation(
            summary = "Obtener formulario para respondente",
            description = "Retorna la estructura del formulario activo con secciones, preguntas y branding del tenant. " +
                    "No requiere autenticación.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Formulario listo para responder")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Formulario no encontrado o no activo", content = @Content)
    public ResponseEntity<ApiResponse<PublicFormResponse>> getForm(@PathVariable UUID formId) {
        PublicFormResult result = getPublicForm.execute(new GetPublicFormQuery(formId));
        return ResponseEntity.ok(ApiResponse.ok(PublicFormResponse.from(result)));
    }

    @PostMapping("/forms/{formId}/responses")
    @Operation(
            summary = "Enviar respuesta anónima",
            description = "Envía una respuesta a un formulario activo sin requerir autenticación. " +
                    "Máximo 10 envíos por IP por minuto. Retorna un token único de respondente.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Respuesta registrada exitosamente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Pregunta obligatoria sin respuesta o datos inválidos", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Formulario no encontrado o no activo", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Límite de envíos por minuto superado", content = @Content)
    public ResponseEntity<ApiResponse<SubmitPublicResponseDto>> submitResponse(
            @PathVariable UUID formId,
            @Valid @RequestBody SubmitResponseRequest request,
            HttpServletRequest httpRequest) {

        if (!rateLimitService.isAllowed(ControllerUtils.clientIp(httpRequest))) {
            throw new BusinessException("error.response.rate_limit", HttpStatus.TOO_MANY_REQUESTS);
        }

        List<AnswerItem> answers = request.answers().stream()
                .map(a -> new AnswerItem(a.questionId(), a.value()))
                .toList();

        SubmitPublicResponseCommand command = new SubmitPublicResponseCommand(
                formId, request.startedAt(), answers);

        SubmitPublicResponseResult result = submitPublicResponse.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new SubmitPublicResponseDto(result.respondentToken())));
    }

}
