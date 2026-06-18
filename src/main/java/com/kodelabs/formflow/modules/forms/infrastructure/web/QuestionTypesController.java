package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.application.service.QuestionTypeRegistry;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.QuestionTypeInfo;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/forms/question-types")
@Tag(name = "Preguntas", description = "CRUD de preguntas dentro de una seccion. Requiere autenticacion.")
@SecurityRequirement(name = "Bearer Auth")
@RequiredArgsConstructor
public class QuestionTypesController {

    private final QuestionTypeRegistry registry;

    @GetMapping
    @Operation(
            summary = "Listar tipos de pregunta disponibles",
            description = "Retorna todos los tipos de pregunta con el esquema esperado del campo 'config'.")
    public ResponseEntity<ApiResponse<List<QuestionTypeInfo>>> list() {
        List<QuestionTypeInfo> types = registry.all().stream()
                .map(h -> new QuestionTypeInfo(h.type(), h.defaultSchema()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(types));
    }
}
