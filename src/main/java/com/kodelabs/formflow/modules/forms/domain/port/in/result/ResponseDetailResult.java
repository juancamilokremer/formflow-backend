package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ResponseDetailResult(
        UUID id,
        UUID formId,
        UUID respondentToken,
        UUID convocatoriaId,
        UUID candidateId,
        Double totalScore,
        FormSnapshot formSnapshot,
        List<AnswerValueResult> answers,
        Instant submittedAt,
        Instant startedAt
) {}
