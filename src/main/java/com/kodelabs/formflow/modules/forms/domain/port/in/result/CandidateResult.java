package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;

import java.time.Instant;
import java.util.UUID;

public record CandidateResult(
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
    public static CandidateResult from(Candidate c) {
        return new CandidateResult(
                c.getId(), c.getConvocatoriaId(), c.getName(), c.getEmail(),
                c.getToken(), c.getStatus().name(), c.getResponseId(),
                c.getScores(), c.getInvitedAt(), c.getRespondedAt(), c.getCreatedAt()
        );
    }
}
