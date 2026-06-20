package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ScoringConfigRequest(
        @Min(0) @Max(100) Integer aptoMin,
        @Min(0) @Max(100) Integer revisarMin
) {}
