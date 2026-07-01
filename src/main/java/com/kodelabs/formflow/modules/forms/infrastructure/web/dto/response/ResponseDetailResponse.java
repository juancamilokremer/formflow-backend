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
        FormSnapshot formSnapshot,
        List<AnswerValueResponse> answers,
        Instant submittedAt,
        Instant startedAt
) {
    public static ResponseDetailResponse from(ResponseDetailResult r) {
        return new ResponseDetailResponse(
                r.id(), r.formId(), r.respondentToken(), r.convocatoriaId(), r.candidateId(),
                r.totalScore(), r.formSnapshot(),
                r.answers().stream().map(AnswerValueResponse::from).toList(),
                r.submittedAt(), r.startedAt());
    }
}
