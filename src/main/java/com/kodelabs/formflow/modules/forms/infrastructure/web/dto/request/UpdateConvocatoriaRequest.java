package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateConvocatoriaRequest(
        @NotBlank @Size(max = 200) String name,
        UUID formId,
        @Valid List<CategoryWeightRequest> categoryWeights,
        @Valid ScoringConfigRequest scoringConfig
) {}
