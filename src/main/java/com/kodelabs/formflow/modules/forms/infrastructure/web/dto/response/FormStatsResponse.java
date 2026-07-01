package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormStatsResult;

import java.util.List;
import java.util.UUID;

public record FormStatsResponse(
        UUID formId,
        String formName,
        int totalResponses,
        List<QuestionStatsResponse> questions
) {
    public static FormStatsResponse from(FormStatsResult r) {
        List<QuestionStatsResponse> questions = r.questions().stream()
                .map(QuestionStatsResponse::from).toList();
        return new FormStatsResponse(r.formId(), r.formName(), r.totalResponses(), questions);
    }
}
