package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateFormExportResult;

import java.util.List;

public record CandidateFormExportResponse(
        String formName,
        Double formScore,
        List<ResponseCategoryScoreResponse> categoryScores,
        List<AnswerDetailResponse> answers
) {
    public static CandidateFormExportResponse from(CandidateFormExportResult r) {
        List<ResponseCategoryScoreResponse> categoryScores = r.categoryScores() == null
                ? null : r.categoryScores().stream().map(ResponseCategoryScoreResponse::from).toList();
        return new CandidateFormExportResponse(
                r.formName(), r.formScore(), categoryScores,
                r.answers().stream().map(AnswerDetailResponse::from).toList());
    }
}
