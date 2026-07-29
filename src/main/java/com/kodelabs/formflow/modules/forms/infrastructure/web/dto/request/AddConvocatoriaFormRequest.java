package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AddConvocatoriaFormRequest(
        @NotNull UUID formId,
        @Min(1) @Max(100) int weight,
        @Valid List<CategoryWeightRequest> categoryWeights,
        @Min(0) @Max(100) Integer minScore
) {}
