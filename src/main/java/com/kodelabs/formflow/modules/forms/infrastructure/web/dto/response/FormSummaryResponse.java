package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;

import java.time.Instant;
import java.util.UUID;

public record FormSummaryResponse(
        UUID id,
        String name,
        String description,
        FormType type,
        FormStatus status,
        int version,
        int sectionCount,
        int responseCount,
        Instant lastResponseAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static FormSummaryResponse from(FormSummaryResult r) {
        return new FormSummaryResponse(
                r.id(), r.name(), r.description(), r.type(), r.status(),
                r.version(), r.sectionCount(), r.responseCount(), r.lastResponseAt(),
                r.createdAt(), r.updatedAt());
    }
}
