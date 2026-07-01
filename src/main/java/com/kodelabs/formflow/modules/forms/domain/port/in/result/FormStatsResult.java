package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.List;
import java.util.UUID;

public record FormStatsResult(
        UUID formId,
        String formName,
        int totalResponses,
        List<QuestionStatsResult> questions
) {}
