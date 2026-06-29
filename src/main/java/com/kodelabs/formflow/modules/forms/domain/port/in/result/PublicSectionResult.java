package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.List;
import java.util.UUID;

public record PublicSectionResult(
        UUID id,
        String title,
        String description,
        int position,
        Integer timeLimitSeconds,
        List<PublicQuestionResult> questions
) {}
