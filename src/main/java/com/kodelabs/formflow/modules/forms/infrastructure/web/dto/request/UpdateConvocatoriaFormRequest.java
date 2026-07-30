package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

public record UpdateConvocatoriaFormRequest(
        @Min(0) @Max(100) int weight,
        @Valid List<CategoryWeightRequest> categoryWeights,
        @Min(0) @Max(100) Integer minScore
) {}
