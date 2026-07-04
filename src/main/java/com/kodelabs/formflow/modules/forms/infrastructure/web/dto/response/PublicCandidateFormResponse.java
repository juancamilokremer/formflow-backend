package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicCandidateFormResult;

import java.time.Instant;

public record PublicCandidateFormResponse(
        String candidateName,
        String convocatoriaName,
        Instant endDate,
        boolean alreadyResponded,
        PublicFormResponse form
) {
    public static PublicCandidateFormResponse from(PublicCandidateFormResult r) {
        return new PublicCandidateFormResponse(
                r.candidateName(),
                r.convocatoriaName(),
                r.endDate(),
                r.alreadyResponded(),
                PublicFormResponse.from(r.form()));
    }
}
