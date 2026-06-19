package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateConvocatoriaRequest(
        @NotBlank @Size(max = 200) String name,
        @NotNull UUID formId,
        @Valid List<CategoryWeightRequest> categoryWeights,
        @Valid ScoringConfigRequest scoringConfig
) {}
