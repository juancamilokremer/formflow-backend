package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;

import java.time.Instant;
import java.util.UUID;

public record FormSummaryResult(
        UUID id,
        String name,
        String description,
        FormType type,
        int version,
        int sectionCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static FormSummaryResult of(Form form, int sectionCount) {
        return new FormSummaryResult(
                form.getId(), form.getName(), form.getDescription(),
                form.getType(), form.getVersion(), sectionCount,
                form.getCreatedAt(), form.getUpdatedAt());
    }
}
