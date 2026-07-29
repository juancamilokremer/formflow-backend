package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidatePortalResult;

import java.time.Instant;
import java.util.List;

public record CandidatePortalResponse(
        String candidateName,
        String convocatoriaName,
        Instant endDate,
        boolean allCompleted,
        List<CandidatePortalFormResponse> forms
) {
    public static CandidatePortalResponse from(CandidatePortalResult r) {
        return new CandidatePortalResponse(
                r.candidateName(), r.convocatoriaName(), r.endDate(), r.allCompleted(),
                r.forms().stream().map(CandidatePortalFormResponse::from).toList());
    }
}
