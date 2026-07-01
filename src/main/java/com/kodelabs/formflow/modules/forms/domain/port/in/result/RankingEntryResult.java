package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateClassification;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RankingEntryResult(
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
) {}
