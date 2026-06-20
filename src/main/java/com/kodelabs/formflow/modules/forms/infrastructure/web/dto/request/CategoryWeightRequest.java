package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CategoryWeightRequest(
        @NotNull UUID categoryId,
        @Min(1) @Max(100) int weight
) {}
