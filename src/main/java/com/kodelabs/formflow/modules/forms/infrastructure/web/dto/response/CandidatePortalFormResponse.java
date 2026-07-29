package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidatePortalFormResult;

import java.util.UUID;

public record CandidatePortalFormResponse(UUID formId, String name, boolean completed) {
    public static CandidatePortalFormResponse from(CandidatePortalFormResult r) {
        return new CandidatePortalFormResponse(r.formId(), r.name(), r.completed());
    }
}
