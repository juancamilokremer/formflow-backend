package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateFormRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @NotNull FormType type,
        Integer timeLimitSeconds
) {}
