package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;

import java.time.Instant;
import java.util.UUID;

public record FormSummaryResult(
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
    public static FormSummaryResult of(Form form, int sectionCount, int responseCount, Instant lastResponseAt) {
        return new FormSummaryResult(
                form.getId(), form.getName(), form.getDescription(),
                form.getType(), form.getStatus(), form.getVersion(),
                sectionCount, responseCount, lastResponseAt,
                form.getCreatedAt(), form.getUpdatedAt());
    }
}
