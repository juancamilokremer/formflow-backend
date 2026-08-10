package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseDetailResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ResponseDetailResponse(
        UUID id,
        UUID formId,
        UUID respondentToken,
        UUID convocatoriaId,
        UUID candidateId,
        Double totalScore,
        List<ResponseCategoryScoreResponse> categoryScores,
        FormSnapshot formSnapshot,
        List<AnswerDetailResponse> answers,
        Instant submittedAt,
        Instant startedAt
) {
    public static ResponseDetailResponse from(ResponseDetailResult r) {
        return new ResponseDetailResponse(
                r.id(), r.formId(), r.respondentToken(), r.convocatoriaId(), r.candidateId(),
                r.totalScore(),
                r.categoryScores() == null ? null : r.categoryScores().stream().map(ResponseCategoryScoreResponse::from).toList(),
                r.formSnapshot(),
                r.answers().stream().map(AnswerDetailResponse::from).toList(),
                r.submittedAt(), r.startedAt());
    }
}
