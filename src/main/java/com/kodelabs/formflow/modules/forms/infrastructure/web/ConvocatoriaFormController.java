package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.port.in.AddConvocatoriaFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.RemoveConvocatoriaFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.ReorderConvocatoriaFormsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateConvocatoriaFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddConvocatoriaFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.RemoveConvocatoriaFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ReorderConvocatoriaFormsCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateConvocatoriaFormCommand;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.AddConvocatoriaFormRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.CategoryWeightRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.ReorderConvocatoriaFormsRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.UpdateConvocatoriaFormRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.ConvocatoriaFormResponse;
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
@RequestMapping("/api/v1/convocatorias/{convocatoriaId}/forms")
@RequiredArgsConstructor
@Tag(name = "Formularios de convocatoria", description = "CRUD de formularios dentro de una convocatoria (peso, categorías, umbral mínimo). Requiere autenticación.")
@SecurityRequirement(name = "Bearer Auth")
public class ConvocatoriaFormController {

    private final AddConvocatoriaFormUseCase addConvocatoriaForm;
    private final UpdateConvocatoriaFormUseCase updateConvocatoriaForm;
    private final RemoveConvocatoriaFormUseCase removeConvocatoriaForm;
    private final ReorderConvocatoriaFormsUseCase reorderConvocatoriaForms;

    @PostMapping
    @Operation(
            summary = "Agregar un formulario a la convocatoria",
            description = "La convocatoria debe estar en estado DRAFT. El peso no se valida contra el resto " +
                    "de formularios en este momento — la suma a 100 se valida recién al lanzar.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Formulario agregado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Convocatoria o formulario no encontrado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "No está en estado DRAFT, o el formulario ya está adjunto", content = @Content)
    public ResponseEntity<ApiResponse<ConvocatoriaFormResponse>> add(
            @PathVariable UUID convocatoriaId,
            @Valid @RequestBody AddConvocatoriaFormRequest request, Authentication auth) {
        var result = addConvocatoriaForm.execute(new AddConvocatoriaFormCommand(
                convocatoriaId, tenantId(), userId(auth), request.formId(), request.weight(),
                toWeightsDomain(request.categoryWeights()), request.minScore()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(ConvocatoriaFormResponse.from(result)));
    }

    @PutMapping("/{convocatoriaFormId}")
    @Operation(
            summary = "Actualizar peso, categorías o umbral mínimo de un formulario de la convocatoria",
            description = "No permite cambiar qué formulario está adjunto — para eso, quitar y agregar de nuevo.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Actualizado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Convocatoria o formulario no encontrado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "No está en estado DRAFT", content = @Content)
    public ResponseEntity<ApiResponse<ConvocatoriaFormResponse>> update(
            @PathVariable UUID convocatoriaId,
            @PathVariable UUID convocatoriaFormId,
            @Valid @RequestBody UpdateConvocatoriaFormRequest request, Authentication auth) {
        var result = updateConvocatoriaForm.execute(new UpdateConvocatoriaFormCommand(
                convocatoriaFormId, convocatoriaId, tenantId(), userId(auth), request.weight(),
                toWeightsDomain(request.categoryWeights()), request.minScore()));
        return ResponseEntity.ok(ApiResponse.ok(ConvocatoriaFormResponse.from(result)));
    }

    @DeleteMapping("/{convocatoriaFormId}")
    @Operation(summary = "Quitar un formulario de la convocatoria", description = "La convocatoria debe estar en estado DRAFT.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Formulario quitado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Convocatoria o formulario no encontrado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "No está en estado DRAFT", content = @Content)
    public ResponseEntity<ApiResponse<Void>> remove(
            @PathVariable UUID convocatoriaId,
            @PathVariable UUID convocatoriaFormId, Authentication auth) {
        removeConvocatoriaForm.execute(new RemoveConvocatoriaFormCommand(
                convocatoriaFormId, convocatoriaId, tenantId(), userId(auth)));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/reorder")
    @Operation(
            summary = "Reordenar los formularios de la convocatoria",
            description = "Recibe la lista completa de IDs de formularios en el nuevo orden. " +
                    "Debe incluir exactamente los mismos IDs que los formularios actuales de la convocatoria.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reordenados, retorna la lista en el nuevo orden")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Los IDs no coinciden exactamente con los formularios actuales", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Convocatoria no encontrada", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "No está en estado DRAFT", content = @Content)
    public ResponseEntity<ApiResponse<List<ConvocatoriaFormResponse>>> reorder(
            @PathVariable UUID convocatoriaId,
            @Valid @RequestBody ReorderConvocatoriaFormsRequest request, Authentication auth) {
        var results = reorderConvocatoriaForms.execute(new ReorderConvocatoriaFormsCommand(
                convocatoriaId, tenantId(), userId(auth), request.orderedConvocatoriaFormIds()));
        return ResponseEntity.ok(ApiResponse.ok(
                results.stream().map(ConvocatoriaFormResponse::from).toList()));
    }

    private List<CategoryWeight> toWeightsDomain(List<CategoryWeightRequest> requests) {
        if (requests == null) return List.of();
        return requests.stream().map(r -> new CategoryWeight(r.categoryId(), r.weight())).toList();
    }
}
