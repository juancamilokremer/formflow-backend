package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.request;

import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateFormStatusRequest(@NotNull FormStatus status) {}
