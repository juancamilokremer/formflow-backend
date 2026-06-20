package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.CloseConvocatoriaUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.CreateConvocatoriaUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.DeleteConvocatoriaUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetConvocatoriaUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.LaunchConvocatoriaUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.ListConvocatoriasUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateConvocatoriaUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CloseConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetConvocatoriaQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.LaunchConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ListConvocatoriasQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.CategoryWeightRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.CreateConvocatoriaRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.ScoringConfigRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.UpdateConvocatoriaRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.ConvocatoriaResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.ConvocatoriaSummaryResponse;
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
@RequestMapping("/api/v1/convocatorias")
@RequiredArgsConstructor
@Tag(name = "Convocatorias", description = "CRUD y ciclo de vida de convocatorias. Requiere autenticación.")
@SecurityRequirement(name = "Bearer Auth")
public class ConvocatoriaController {

    private final CreateConvocatoriaUseCase createConvocatoria;
    private final UpdateConvocatoriaUseCase updateConvocatoria;
    private final GetConvocatoriaUseCase getConvocatoria;
    private final ListConvocatoriasUseCase listConvocatorias;
    private final DeleteConvocatoriaUseCase deleteConvocatoria;
    private final LaunchConvocatoriaUseCase launchConvocatoria;
    private final CloseConvocatoriaUseCase closeConvocatoria;

    @PostMapping
    @Operation(summary = "Crear convocatoria", description = "Crea una nueva convocatoria en estado DRAFT.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Convocatoria creada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Formulario no encontrado", content = @Content)
    public ResponseEntity<ApiResponse<ConvocatoriaResponse>> create(
            @Valid @RequestBody CreateConvocatoriaRequest request, Authentication auth) {
        var result = createConvocatoria.execute(new CreateConvocatoriaCommand(
                tenantId(), userId(auth), request.formId(), request.name(),
                toWeightsDomain(request.categoryWeights()),
                toScoringDomain(request.scoringConfig())));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(ConvocatoriaResponse.from(result)));
    }

    @GetMapping
    @Operation(summary = "Listar convocatorias", description = "Retorna todas las convocatorias activas del tenant.")
    public ResponseEntity<ApiResponse<List<ConvocatoriaSummaryResponse>>> list() {
        var results = listConvocatorias.execute(new ListConvocatoriasQuery(tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(
                results.stream().map(ConvocatoriaSummaryResponse::from).toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener convocatoria", description = "Retorna detalle completo incluyendo candidatos.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Convocatoria encontrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    public ResponseEntity<ApiResponse<ConvocatoriaResponse>> get(@PathVariable UUID id) {
        var result = getConvocatoria.execute(new GetConvocatoriaQuery(id, tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(ConvocatoriaResponse.from(result)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar convocatoria", description = "Solo convocatorias en estado DRAFT pueden modificarse.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Actualizada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "No está en estado DRAFT", content = @Content)
    public ResponseEntity<ApiResponse<ConvocatoriaResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateConvocatoriaRequest request, Authentication auth) {
        var result = updateConvocatoria.execute(new UpdateConvocatoriaCommand(
                id, tenantId(), userId(auth), request.name(),
                toWeightsDomain(request.categoryWeights()),
                toScoringDomain(request.scoringConfig())));
        return ResponseEntity.ok(ApiResponse.ok(ConvocatoriaResponse.from(result)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar convocatoria", description = "Soft delete. Solo convocatorias DRAFT pueden eliminarse.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Eliminada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "No está en estado DRAFT", content = @Content)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id, Authentication auth) {
        deleteConvocatoria.execute(new DeleteConvocatoriaCommand(id, tenantId(), userId(auth)));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/launch")
    @Operation(summary = "Lanzar convocatoria", description = "Cambia estado de DRAFT a ACTIVE. Requiere al menos un candidato.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lanzada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Sin candidatos o pesos inválidos", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "No está en estado DRAFT", content = @Content)
    public ResponseEntity<ApiResponse<ConvocatoriaResponse>> launch(@PathVariable UUID id, Authentication auth) {
        var result = launchConvocatoria.execute(new LaunchConvocatoriaCommand(id, tenantId(), userId(auth)));
        return ResponseEntity.ok(ApiResponse.ok(ConvocatoriaResponse.from(result)));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Cerrar convocatoria", description = "Cambia estado de ACTIVE a CLOSED.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cerrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "No está en estado ACTIVE", content = @Content)
    public ResponseEntity<ApiResponse<ConvocatoriaResponse>> close(@PathVariable UUID id, Authentication auth) {
        var result = closeConvocatoria.execute(new CloseConvocatoriaCommand(id, tenantId(), userId(auth)));
        return ResponseEntity.ok(ApiResponse.ok(ConvocatoriaResponse.from(result)));
    }

    private List<CategoryWeight> toWeightsDomain(List<CategoryWeightRequest> requests) {
        if (requests == null) return List.of();
        return requests.stream()
                .map(r -> new CategoryWeight(r.categoryId(), r.weight()))
                .toList();
    }

    private ScoringConfig toScoringDomain(ScoringConfigRequest request) {
        if (request == null) return ScoringConfig.defaults();
        int aptoMin    = request.aptoMin()    != null ? request.aptoMin()    : 70;
        int revisarMin = request.revisarMin() != null ? request.revisarMin() : 50;
        return new ScoringConfig(aptoMin, revisarMin);
    }
}
