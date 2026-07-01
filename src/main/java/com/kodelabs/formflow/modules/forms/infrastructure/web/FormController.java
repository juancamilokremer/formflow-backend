package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.CreateFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.DeleteFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetFormScoringUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetFormStatsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.ListFormsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateFormStatusUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormScoringQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormStatsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ListFormsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateFormStatusCommand;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.CreateFormRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.UpdateFormRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.UpdateFormStatusRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.FormDetailResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.FormScoringResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.FormStatsResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.FormSummaryResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
@Tag(name = "Formularios", description = "CRUD de formularios. Requiere autenticacion.")
@SecurityRequirement(name = "Bearer Auth")
public class FormController {

    private final CreateFormUseCase createForm;
    private final ListFormsUseCase listForms;
    private final GetFormUseCase getForm;
    private final UpdateFormUseCase updateForm;
    private final UpdateFormStatusUseCase updateFormStatus;
    private final DeleteFormUseCase deleteForm;
    private final GetFormScoringUseCase getFormScoring;
    private final GetFormStatsUseCase getFormStats;

    @PostMapping
    @Operation(
            summary = "Crear un nuevo formulario",
            description = "Crea un formulario vacio (sin secciones) para el tenant activo. " +
                    "El tipo determina si el formulario soporta scoring (CANDIDATES, DIAGNOSTIC) " +
                    "o es solo recoleccion de datos (REGISTRATION).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "Formulario creado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Datos de entrada invalidos", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content)
    public ResponseEntity<ApiResponse<FormSummaryResponse>> create(
            @Valid @RequestBody CreateFormRequest request, Authentication auth) {
        var result = createForm.execute(new CreateFormCommand(
                tenantId(), userId(auth), request.name(), request.description(),
                request.type(), request.timeLimitSeconds()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(FormSummaryResponse.from(result)));
    }

    @GetMapping
    @Operation(
            summary = "Listar formularios del tenant",
            description = "Retorna todos los formularios activos (no eliminados) del tenant. " +
                    "Incluye el conteo de secciones de cada formulario.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Lista de formularios")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content)
    public ResponseEntity<ApiResponse<List<FormSummaryResponse>>> list() {
        var results = listForms.execute(new ListFormsQuery(tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(
                results.stream().map(FormSummaryResponse::from).toList()));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener un formulario con sus secciones",
            description = "Retorna el formulario con la lista completa de secciones activas ordenadas por posicion.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Formulario con secciones")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Formulario no encontrado o no pertenece al tenant", content = @Content)
    public ResponseEntity<ApiResponse<FormDetailResponse>> get(@PathVariable UUID id) {
        var result = getForm.execute(new GetFormQuery(id, tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(FormDetailResponse.from(result)));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar metadatos del formulario",
            description = "Actualiza nombre, descripcion y tiempo limite. No incrementa la version " +
                    "del formulario — solo los cambios estructurales (secciones) lo hacen.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Formulario actualizado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Datos de entrada invalidos", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Formulario no encontrado o no pertenece al tenant", content = @Content)
    public ResponseEntity<ApiResponse<FormSummaryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFormRequest request, Authentication auth) {
        var result = updateForm.execute(new UpdateFormCommand(
                id, tenantId(), userId(auth), request.name(),
                request.description(), request.timeLimitSeconds()));
        return ResponseEntity.ok(ApiResponse.ok(FormSummaryResponse.from(result)));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Cambiar estado del formulario",
            description = "Cambia el estado del formulario entre DRAFT, ACTIVE y ARCHIVED. " +
                    "Solo un formulario ACTIVE acepta respuestas públicas.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Estado actualizado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Estado inválido", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Formulario no encontrado", content = @Content)
    public ResponseEntity<ApiResponse<FormSummaryResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFormStatusRequest request, Authentication auth) {
        var result = updateFormStatus.execute(
                new UpdateFormStatusCommand(id, tenantId(), userId(auth), request.status()));
        return ResponseEntity.ok(ApiResponse.ok(FormSummaryResponse.from(result)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un formulario (soft delete)",
            description = "Marca el formulario como eliminado sin borrar datos historicos. " +
                    "Las respuestas existentes y sus snapshots se conservan.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Formulario eliminado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Formulario no encontrado o no pertenece al tenant", content = @Content)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deleteForm.execute(new DeleteFormCommand(id, tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

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
