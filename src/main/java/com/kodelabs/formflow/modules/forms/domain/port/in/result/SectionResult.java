package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.FormSection;

import java.time.Instant;
import java.util.UUID;

public record SectionResult(
        UUID id,
        UUID formId,
        String title,
        String description,
        int position,
        Instant createdAt,
        Instant updatedAt
) {
    public static SectionResult from(FormSection s) {
        return new SectionResult(
                s.getId(), s.getFormId(), s.getTitle(), s.getDescription(),
                s.getPosition(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
