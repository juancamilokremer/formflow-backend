package com.kodelabs.formflow.modules.forms.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSectionRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description
) {}
