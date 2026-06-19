package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.CategoryResult;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String color,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public static CategoryResponse from(CategoryResult r) {
        return new CategoryResponse(r.id(), r.name(), r.color(), r.description(),
                r.createdAt(), r.updatedAt());
    }
}
