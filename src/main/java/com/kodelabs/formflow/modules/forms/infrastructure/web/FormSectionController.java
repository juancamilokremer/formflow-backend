package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.AddSectionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.DeleteSectionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.ReorderSectionsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateSectionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddSectionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteSectionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ReorderSectionsCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateSectionCommand;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.CreateSectionRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.ReorderSectionsRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.SectionResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.UpdateSectionRequest;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/v1/forms/{formId}/sections")
@RequiredArgsConstructor
@Tag(name = "Secciones", description = "CRUD de secciones de un formulario. Requiere autenticacion.")
@SecurityRequirement(name = "Bearer Auth")
public class FormSectionController {

    private final AddSectionUseCase addSection;
    private final UpdateSectionUseCase updateSection;
    private final DeleteSectionUseCase deleteSection;
    private final ReorderSectionsUseCase reorderSections;

    @PostMapping
    @Operation(
            summary = "Agregar una seccion al formulario",
            description = "La seccion se agrega al final (posicion = numero de secciones activas actuales). " +
                    "Incrementa la version del formulario.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "Seccion creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Datos de entrada invalidos", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Formulario no encontrado o no pertenece al tenant", content = @Content)
    })
    public ResponseEntity<ApiResponse<SectionResponse>> add(
            @PathVariable UUID formId,
            @Valid @RequestBody CreateSectionRequest request, Authentication auth) {
        var result = addSection.execute(new AddSectionCommand(
                formId, tenantId(), userId(auth), request.title(), request.description()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(SectionResponse.from(result)));
    }

    @PutMapping("/{sectionId}")
    @Operation(
            summary = "Actualizar titulo y descripcion de una seccion",
            description = "Actualiza solo metadatos de la seccion. No afecta la version del formulario.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Seccion actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Datos de entrada invalidos", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Seccion no encontrada en el formulario del tenant", content = @Content)
    })
    public ResponseEntity<ApiResponse<SectionResponse>> update(
            @PathVariable UUID formId,
            @PathVariable UUID sectionId,
            @Valid @RequestBody UpdateSectionRequest request) {
        var result = updateSection.execute(new UpdateSectionCommand(
                sectionId, formId, tenantId(), request.title(), request.description()));
        return ResponseEntity.ok(ApiResponse.ok(SectionResponse.from(result)));
    }

    @DeleteMapping("/{sectionId}")
    @Operation(
            summary = "Eliminar una seccion (soft delete)",
            description = "Marca la seccion como eliminada sin borrar preguntas ni respuestas historicas. " +
                    "Incrementa la version del formulario.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Seccion eliminada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Seccion no encontrada en el formulario del tenant", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID formId,
            @PathVariable UUID sectionId, Authentication auth) {
        deleteSection.execute(new DeleteSectionCommand(sectionId, formId, tenantId(), userId(auth)));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/reorder")
    @Operation(
            summary = "Reordenar secciones del formulario",
            description = "Recibe la lista completa de IDs de secciones activas en el nuevo orden. " +
                    "Debe incluir exactamente los mismos IDs que las secciones activas actuales — ni mas ni menos. " +
                    "Incrementa la version del formulario.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Secciones reordenadas, retorna la lista en el nuevo orden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Los IDs no coinciden exactamente con las secciones activas", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Formulario no encontrado o no pertenece al tenant", content = @Content)
    })
    public ResponseEntity<ApiResponse<List<SectionResponse>>> reorder(
            @PathVariable UUID formId,
            @Valid @RequestBody ReorderSectionsRequest request, Authentication auth) {
        var results = reorderSections.execute(new ReorderSectionsCommand(
                formId, tenantId(), userId(auth), request.orderedSectionIds()));
        return ResponseEntity.ok(ApiResponse.ok(
                results.stream().map(SectionResponse::from).toList()));
    }
}
