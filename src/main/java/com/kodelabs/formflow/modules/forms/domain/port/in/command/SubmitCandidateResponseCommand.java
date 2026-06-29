package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SubmitCandidateResponseCommand(
        UUID candidateToken,
        Instant startedAt,
        List<AnswerItem> answers
) {}
