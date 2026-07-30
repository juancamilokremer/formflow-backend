package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.UUID;

public record RankingFormScoreResult(UUID formId, String formName, int weight, Double score, boolean completed) {}
