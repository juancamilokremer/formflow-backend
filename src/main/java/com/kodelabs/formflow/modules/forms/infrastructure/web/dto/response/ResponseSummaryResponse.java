package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.ResponseSummaryResult;

import java.time.Instant;
import java.util.UUID;

public record ResponseSummaryResponse(
        UUID id,
        UUID respondentToken,
        UUID convocatoriaId,
        UUID candidateId,
        Double totalScore,
        Instant submittedAt,
        Instant startedAt
) {
    public static ResponseSummaryResponse from(ResponseSummaryResult r) {
        return new ResponseSummaryResponse(
                r.id(), r.respondentToken(), r.convocatoriaId(), r.candidateId(),
                r.totalScore(), r.submittedAt(), r.startedAt());
    }
}
