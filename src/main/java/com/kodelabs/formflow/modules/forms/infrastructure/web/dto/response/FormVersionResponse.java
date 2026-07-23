package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormVersionResult;

import java.time.Instant;
import java.util.UUID;

public record FormVersionResponse(
        UUID id,
        int version,
        FormStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static FormVersionResponse from(FormVersionResult r) {
        return new FormVersionResponse(r.id(), r.version(), r.status(), r.createdAt(), r.updatedAt());
    }
}
