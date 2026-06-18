package com.kodelabs.formflow.modules.forms.infrastructure.web;

import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.infrastructure.web.dto.QuestionTypeInfo;
import com.kodelabs.formflow.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/forms/question-types")
@Tag(name = "Preguntas", description = "CRUD de preguntas dentro de una seccion. Requiere autenticacion.")
@SecurityRequirement(name = "Bearer Auth")
public class QuestionTypesController {

    private static final List<QuestionTypeInfo> TYPES = List.of(
            new QuestionTypeInfo(QuestionType.TEXT,
                    Map.of("maxLength", 2000, "placeholder", "", "rows", 1)),
            new QuestionTypeInfo(QuestionType.SINGLE,
                    Map.of("options", List.of(Map.of("id", "uuid", "label", "Option", "score", 0)), "randomize", false)),
            new QuestionTypeInfo(QuestionType.MULTIPLE,
                    Map.of("options", List.of(Map.of("id", "uuid", "label", "Option", "score", 0)), "randomize", false)),
            new QuestionTypeInfo(QuestionType.SCALE,
                    Map.of("min", 1, "max", 5, "minLabel", "", "maxLabel", "", "scoringType", "NONE")),
            new QuestionTypeInfo(QuestionType.DATE,
                    Map.of("includeTime", false)),
            new QuestionTypeInfo(QuestionType.FILE,
                    Map.of("maxSizeMb", 5, "allowedTypes", List.of("pdf", "jpg", "png"))),
            new QuestionTypeInfo(QuestionType.MATRIX,
                    Map.of("rows", List.of(Map.of("id", "uuid", "label", "Row")),
                            "columns", List.of(Map.of("id", "uuid", "label", "Column", "score", 0)))),
            new QuestionTypeInfo(QuestionType.NPS,
                    Map.of("minLabel", "", "maxLabel", ""))
    );

    @GetMapping
    @Operation(
            summary = "Listar tipos de pregunta disponibles",
            description = "Retorna todos los tipos de pregunta con el esquema esperado del campo 'config'.")
    public ResponseEntity<ApiResponse<List<QuestionTypeInfo>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(TYPES));
    }
}
