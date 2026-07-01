package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.List;
import java.util.UUID;

public record QuestionStatsResult(
        UUID questionId,
        String title,
        String type,
        int totalResponses,
        int answeredCount,
        List<OptionDistribution> distributions,
        Double average,
        Double median,
        Double npsScore,
        List<MatrixRowStats> matrixRows
) {}
