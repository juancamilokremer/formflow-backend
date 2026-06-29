package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicFormResult;

import java.util.List;
import java.util.UUID;

public record PublicFormResponse(
        UUID formId,
        String name,
        String type,
        Integer timeLimitSeconds,
        String tenantName,
        String tenantLogoUrl,
        String tenantPrimaryColor,
        List<PublicSectionResponse> sections
) {
    public static PublicFormResponse from(PublicFormResult r) {
        return new PublicFormResponse(
                r.formId(), r.name(),
                r.type() != null ? r.type().name() : null,
                r.timeLimitSeconds(),
                r.tenantName(), r.tenantLogoUrl(), r.tenantPrimaryColor(),
                r.sections().stream().map(PublicSectionResponse::from).toList());
    }
}
