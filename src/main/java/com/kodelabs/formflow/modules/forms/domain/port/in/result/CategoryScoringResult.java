package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.UUID;

public record CategoryScoringResult(
        UUID categoryId,
        String categoryName,
        String categoryColor,
        int questionCount,
        int maxScore
) {}
