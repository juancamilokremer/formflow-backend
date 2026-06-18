package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryScoringResult;

import java.util.UUID;

public record CategoryScoringResponse(
        UUID categoryId,
        String categoryName,
        String categoryColor,
        int questionCount,
        int maxScore
) {
    public static CategoryScoringResponse from(CategoryScoringResult r) {
        return new CategoryScoringResponse(r.categoryId(), r.categoryName(), r.categoryColor(),
                r.questionCount(), r.maxScore());
    }
}
