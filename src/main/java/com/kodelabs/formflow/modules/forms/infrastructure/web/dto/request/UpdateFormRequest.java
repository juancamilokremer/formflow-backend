package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFormRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        Integer timeLimitSeconds
) {}
