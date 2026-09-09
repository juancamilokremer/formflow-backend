package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormQuestionStatsResult;

import java.util.List;
import java.util.UUID;

public record FormQuestionStatsResponse(
        UUID formId,
        String formName,
        int totalResponses,
        List<QuestionStatsResponse> questions
) {
    public static FormQuestionStatsResponse from(FormQuestionStatsResult r) {
        List<QuestionStatsResponse> questions = r.questions().stream()
                .map(QuestionStatsResponse::from).toList();
        return new FormQuestionStatsResponse(r.formId(), r.formName(), r.totalResponses(), questions);
    }
}
