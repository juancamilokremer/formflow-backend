package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.port.in.CreateCategoryUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.DeleteCategoryUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.GetCategoryUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.ListCategoriesUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateCategoryUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateCategoryCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteCategoryCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetCategoryQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ListCategoriesQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateCategoryCommand;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.CreateCategoryRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request.UpdateCategoryRequest;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response.CategoryResponse;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "CRUD de categorías para scoring de formularios. Requiere autenticación.")
@SecurityRequirement(name = "Bearer Auth")
public class CategoryController {

    private final CreateCategoryUseCase createCategory;
    private final UpdateCategoryUseCase updateCategory;
    private final DeleteCategoryUseCase deleteCategory;
    private final GetCategoryUseCase getCategory;
    private final ListCategoriesUseCase listCategories;

    @PostMapping
    @Operation(
            summary = "Crear una categoría",
            description = "Crea una nueva categoría para el tenant activo. " +
                    "Las categorías se usan para agrupar preguntas y configurar pesos en las convocatorias.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "Categoría creada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Datos de entrada inválidos", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Ya existe una categoría con ese nombre", content = @Content)
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CreateCategoryRequest request) {
        var result = createCategory.execute(new CreateCategoryCommand(
                tenantId(), request.name(), request.color(), request.description()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(CategoryResponse.from(result)));
    }

    @GetMapping
    @Operation(
            summary = "Listar categorías del tenant",
            description = "Retorna todas las categorías del tenant ordenadas por nombre.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Lista de categorías")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content)
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> list() {
        var results = listCategories.execute(new ListCategoriesQuery(tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(
                results.stream().map(CategoryResponse::from).toList()));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener una categoría",
            description = "Retorna los datos de una categoría específica del tenant.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Categoría encontrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Categoría no encontrada", content = @Content)
    public ResponseEntity<ApiResponse<CategoryResponse>> get(@PathVariable UUID id) {
        var result = getCategory.execute(new GetCategoryQuery(id, tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(CategoryResponse.from(result)));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar una categoría",
            description = "Actualiza nombre, color y descripción de la categoría.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Categoría actualizada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Datos de entrada inválidos", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Categoría no encontrada", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Ya existe una categoría con ese nombre", content = @Content)
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request) {
        var result = updateCategory.execute(new UpdateCategoryCommand(
                id, tenantId(), request.name(), request.color(), request.description()));
        return ResponseEntity.ok(ApiResponse.ok(CategoryResponse.from(result)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar una categoría",
            description = "Elimina permanentemente la categoría. " +
                    "Falla con 409 si alguna pregunta activa está asignada a ella.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Categoría eliminada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "No autenticado", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Categoría no encontrada", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "La categoría tiene preguntas asignadas", content = @Content)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deleteCategory.execute(new DeleteCategoryCommand(id, tenantId()));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
