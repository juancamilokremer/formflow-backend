package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateSectionRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @Positive Integer timeLimitSeconds
) {}
