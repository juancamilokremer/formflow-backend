package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SubmitPublicResponseCommand(
        UUID formId,
        Instant startedAt,
        List<AnswerItem> answers
) {}
