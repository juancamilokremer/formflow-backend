package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.Category;

import java.time.Instant;
import java.util.UUID;

public record CategoryResult(
        UUID id,
        String name,
        String color,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public static CategoryResult from(Category c) {
        return new CategoryResult(c.getId(), c.getName(), c.getColor(),
                c.getDescription(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
