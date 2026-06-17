package com.kodelabs.formflow.modules.forms.infrastructure.web.dto;

import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;

import java.time.Instant;
import java.util.UUID;

public record FormSummaryResponse(
        UUID id,
        String name,
        String description,
        FormType type,
        int version,
        int sectionCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static FormSummaryResponse from(FormSummaryResult r) {
        return new FormSummaryResponse(
                r.id(), r.name(), r.description(), r.type(),
                r.version(), r.sectionCount(), r.createdAt(), r.updatedAt());
    }
}
