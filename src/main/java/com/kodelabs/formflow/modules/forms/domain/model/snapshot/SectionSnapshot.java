package com.kodelabs.formflow.modules.forms.domain.model.snapshot;

import java.util.List;
import java.util.UUID;

public record SectionSnapshot(
        UUID id,
        String title,
        String description,
        int position,
        Integer timeLimitSeconds,
        List<QuestionSnapshot> questions
) {}
