package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.RankingFormScoreResult;

import java.util.UUID;

public record RankingFormScoreResponse(UUID formId, String formName, int weight, Double score, boolean completed) {
    public static RankingFormScoreResponse from(RankingFormScoreResult r) {
        return new RankingFormScoreResponse(r.formId(), r.formName(), r.weight(), r.score(), r.completed());
    }
}
