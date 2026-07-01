package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;

import java.util.List;
import java.util.UUID;

public record QuestionStatsResponse(
        UUID questionId,
        String title,
        String type,
        int totalResponses,
        int answeredCount,
        List<OptionDistributionResponse> distributions,
        Double average,
        Double median,
        Double npsScore,
        List<MatrixRowStatsResponse> matrixRows
) {
    public static QuestionStatsResponse from(QuestionStatsResult r) {
        List<OptionDistributionResponse> dists = r.distributions() != null
                ? r.distributions().stream().map(OptionDistributionResponse::from).toList()
                : null;
        List<MatrixRowStatsResponse> rows = r.matrixRows() != null
                ? r.matrixRows().stream().map(MatrixRowStatsResponse::from).toList()
                : null;
        return new QuestionStatsResponse(
                r.questionId(), r.title(), r.type(),
                r.totalResponses(), r.answeredCount(),
                dists, r.average(), r.median(), r.npsScore(), rows);
    }
}
