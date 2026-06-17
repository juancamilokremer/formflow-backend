package com.kodelabs.formflow.modules.forms.infrastructure.web.dto;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.SectionResult;

import java.time.Instant;
import java.util.UUID;

public record SectionResponse(
        UUID id,
        UUID formId,
        String title,
        String description,
        int position,
        Instant createdAt,
        Instant updatedAt
) {
    public static SectionResponse from(SectionResult r) {
        return new SectionResponse(
                r.id(), r.formId(), r.title(), r.description(),
                r.position(), r.createdAt(), r.updatedAt());
    }
}
