package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormScoringResult;

import java.util.List;
import java.util.UUID;

public record FormScoringResponse(
        UUID formId,
        int totalMaxScore,
        List<CategoryScoringResponse> categories
) {
    public static FormScoringResponse from(FormScoringResult r) {
        List<CategoryScoringResponse> cats = r.categories().stream()
                .map(CategoryScoringResponse::from).toList();
        return new FormScoringResponse(r.formId(), r.totalMaxScore(), cats);
    }
}
