package com.kodelabs.formflow.modules.forms.infrastructure.web.dto;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormDetailResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SectionResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FormDetailResponse(
        UUID id,
        String name,
        String description,
        FormType type,
        int version,
        Integer timeLimitSeconds,
        List<SectionResponse> sections,
        Instant createdAt,
        Instant updatedAt
) {
    public static FormDetailResponse from(FormDetailResult result) {
        Form f = result.form();
        List<SectionResponse> sections = f.getSections().stream()
                .map(s -> SectionResponse.from(SectionResult.from(s)))
                .toList();
        return new FormDetailResponse(
                f.getId(), f.getName(), f.getDescription(), f.getType(),
                f.getVersion(), f.getTimeLimitSeconds(), sections,
                f.getCreatedAt(), f.getUpdatedAt());
    }
}
