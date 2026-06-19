package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.List;
import java.util.UUID;

public record FormScoringResult(
        UUID formId,
        int totalMaxScore,
        List<CategoryScoringResult> categories
) {}
