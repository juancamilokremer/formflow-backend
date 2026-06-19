package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateConvocatoriaRequest(
        @NotBlank @Size(max = 200) String name,
        @Valid List<CategoryWeightRequest> categoryWeights,
        @Valid ScoringConfigRequest scoringConfig
) {}
