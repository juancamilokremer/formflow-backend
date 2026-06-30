package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.time.Instant;
import java.util.UUID;

public record ResponseSummaryResult(
        UUID id,
        UUID respondentToken,
        UUID convocatoriaId,
        UUID candidateId,
        Double totalScore,
        Instant submittedAt,
        Instant startedAt
) {}
