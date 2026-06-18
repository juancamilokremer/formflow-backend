package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.SectionResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SectionResponse(
        UUID id,
        UUID formId,
        String title,
        String description,
        int position,
        Integer timeLimitSeconds,
        List<QuestionResponse> questions,
        Instant createdAt,
        Instant updatedAt
) {
    public static SectionResponse from(SectionResult r) {
        List<QuestionResponse> questions = r.questions().stream()
                .map(QuestionResponse::from).toList();
        return new SectionResponse(
                r.id(), r.formId(), r.title(), r.description(),
                r.position(), r.timeLimitSeconds(), questions, r.createdAt(), r.updatedAt());
    }
}
