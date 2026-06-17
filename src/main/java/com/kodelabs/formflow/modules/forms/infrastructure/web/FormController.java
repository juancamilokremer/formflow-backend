package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.AddSectionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.CreateFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.DeleteFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.DeleteSectionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.ListFormsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.ReorderSectionsUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateSectionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddSectionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteSectionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ListFormsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ReorderSectionsCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateSectionCommand;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.CreateFormRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.CreateSectionRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.FormDetailResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.FormSummaryResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.ReorderSectionsRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.SectionResponse;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.UpdateFormRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.UpdateSectionRequest;
import com.kodelabs.formflow.shared.tenant.TenantContext;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
@Tag(name = "Formularios", description = "CRUD de formularios y secciones. Requiere autenticación.")
@SecurityRequirement(name = "Bearer Auth")
public class FormController {

    private final CreateFormUseCase createForm;
    private final ListFormsUseCase listForms;
    private final GetFormUseCase getForm;
    private final UpdateFormUseCase updateForm;
    private final DeleteFormUseCase deleteForm;
    private final AddSectionUseCase addSection;
    private final UpdateSectionUseCase updateSection;
    private final DeleteSectionUseCase deleteSection;
    private final ReorderSectionsUseCase reorderSections;

    // ── Forms ────────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Crear un nuevo formulario")
    public ResponseEntity<ApiResponse<FormSummaryResponse>> create(
            @Valid @RequestBody CreateFormRequest request, Authentication auth) {
        UUID tenantId = tenantId();
        UUID userId = userId(auth);
        var result = createForm.execute(new CreateFormCommand(
                tenantId, userId, request.name(), request.description(),
                request.type(), request.timeLimitSeconds()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(FormSummaryResponse.from(result)));
    }

    @GetMapping
    @Operation(summary = "Listar formularios del tenant")
    public ResponseEntity<ApiResponse<List<FormSummaryResponse>>> list() {
        var results = listForms.execute(new ListFormsQuery(tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(
                results.stream().map(FormSummaryResponse::from).toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un formulario con sus secciones")
    public ResponseEntity<ApiResponse<FormDetailResponse>> get(@PathVariable UUID id) {
        var result = getForm.execute(new GetFormQuery(id, tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(FormDetailResponse.from(result)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar metadatos de un formulario")
    public ResponseEntity<ApiResponse<FormSummaryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFormRequest request, Authentication auth) {
        var result = updateForm.execute(new UpdateFormCommand(
                id, tenantId(), userId(auth), request.name(),
                request.description(), request.timeLimitSeconds()));
        return ResponseEntity.ok(ApiResponse.ok(FormSummaryResponse.from(result)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar (soft delete) un formulario")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deleteForm.execute(new DeleteFormCommand(id, tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ── Sections ─────────────────────────────────────────────────────────────

    @PostMapping("/{formId}/sections")
    @Operation(summary = "Agregar una sección al formulario")
    public ResponseEntity<ApiResponse<SectionResponse>> addSection(
            @PathVariable UUID formId,
            @Valid @RequestBody CreateSectionRequest request, Authentication auth) {
        var result = addSection.execute(new AddSectionCommand(
                formId, tenantId(), userId(auth), request.title(), request.description()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(SectionResponse.from(result)));
    }

    @PutMapping("/{formId}/sections/{sectionId}")
    @Operation(summary = "Actualizar una sección")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(
            @PathVariable UUID formId, @PathVariable UUID sectionId,
            @Valid @RequestBody UpdateSectionRequest request) {
        var result = updateSection.execute(new UpdateSectionCommand(
                sectionId, formId, tenantId(), request.title(), request.description()));
        return ResponseEntity.ok(ApiResponse.ok(SectionResponse.from(result)));
    }

    @DeleteMapping("/{formId}/sections/{sectionId}")
    @Operation(summary = "Eliminar (soft delete) una sección")
    public ResponseEntity<ApiResponse<Void>> deleteSection(
            @PathVariable UUID formId, @PathVariable UUID sectionId, Authentication auth) {
        deleteSection.execute(new DeleteSectionCommand(sectionId, formId, tenantId(), userId(auth)));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/{formId}/sections/reorder")
    @Operation(summary = "Reordenar secciones del formulario")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> reorder(
            @PathVariable UUID formId,
            @Valid @RequestBody ReorderSectionsRequest request, Authentication auth) {
        var results = reorderSections.execute(new ReorderSectionsCommand(
                formId, tenantId(), userId(auth), request.orderedSectionIds()));
        return ResponseEntity.ok(ApiResponse.ok(
                results.stream().map(SectionResponse::from).toList()));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private UUID tenantId() {
        return UUID.fromString(TenantContext.getTenantId());
    }

    private UUID userId(Authentication auth) {
        return UUID.fromString((String) auth.getPrincipal());
    }
}
