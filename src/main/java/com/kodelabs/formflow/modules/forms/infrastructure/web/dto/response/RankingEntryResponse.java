package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateClassification;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.RankingEntryResult;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RankingEntryResponse(
        UUID candidateId,
        String name,
        String email,
        UUID token,
        String status,
        UUID responseId,
        Integer rank,
        Double totalScore,
        CandidateClassification classification,
        Map<String, Double> scoresByCategory,
        Instant respondedAt
) {
    public static RankingEntryResponse from(RankingEntryResult r) {
        return new RankingEntryResponse(
                r.candidateId(), r.name(), r.email(), r.token(),
                r.status(), r.responseId(), r.rank(), r.totalScore(),
                r.classification(), r.scoresByCategory(), r.respondedAt()
        );
    }
}
