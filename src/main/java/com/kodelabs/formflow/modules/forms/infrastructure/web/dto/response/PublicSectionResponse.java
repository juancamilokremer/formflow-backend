package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicSectionResult;

import java.util.List;
import java.util.UUID;

public record PublicSectionResponse(
        UUID id,
        String title,
        String description,
        int position,
        Integer timeLimitSeconds,
        List<PublicQuestionResponse> questions
) {
    public static PublicSectionResponse from(PublicSectionResult r) {
        return new PublicSectionResponse(
                r.id(), r.title(), r.description(), r.position(), r.timeLimitSeconds(),
                r.questions().stream().map(PublicQuestionResponse::from).toList());
    }
}
