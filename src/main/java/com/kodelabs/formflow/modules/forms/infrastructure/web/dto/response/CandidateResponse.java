package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateResult;

import java.time.Instant;
import java.util.UUID;

public record CandidateResponse(
        UUID id,
        UUID convocatoriaId,
        String name,
        String email,
        UUID token,
        String status,
        UUID responseId,
        CandidateScores scores,
        Instant invitedAt,
        Instant respondedAt,
        Instant createdAt
) {
    public static CandidateResponse from(CandidateResult r) {
        return new CandidateResponse(
                r.id(), r.convocatoriaId(), r.name(), r.email(), r.token(),
                r.status(), r.responseId(), r.scores(),
                r.invitedAt(), r.respondedAt(), r.createdAt()
        );
    }
}
