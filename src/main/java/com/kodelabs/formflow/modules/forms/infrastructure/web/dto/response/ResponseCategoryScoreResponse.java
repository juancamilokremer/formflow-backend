package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseCategoryScoreResult;

import java.util.UUID;

public record ResponseCategoryScoreResponse(UUID categoryId, String categoryName, double score) {
    public static ResponseCategoryScoreResponse from(ResponseCategoryScoreResult r) {
        return new ResponseCategoryScoreResponse(r.categoryId(), r.categoryName(), r.score());
    }
}
