package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.List;

public record CandidateFormExportResult(
        String formName,
        Double formScore,
        List<ResponseCategoryScoreResult> categoryScores,
        List<AnswerDetailResult> answers) {}
