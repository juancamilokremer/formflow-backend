package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;

import java.time.Instant;
import java.util.UUID;

public record FormVersionResult(
        UUID id,
        int version,
        FormStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static FormVersionResult from(Form form) {
        return new FormVersionResult(
                form.getId(), form.getVersion(), form.getStatus(),
                form.getCreatedAt(), form.getUpdatedAt());
    }
}
