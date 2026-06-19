package com.kodelabs.formflow.modules.forms.domain.model.snapshot;

import java.util.Map;
import java.util.UUID;

public record QuestionSnapshot(
        UUID id,
        String title,
        String description,
        String type,
        int position,
        boolean required,
        UUID categoryId,
        Integer timeLimitSeconds,
        Map<String, Object> config
) {}
