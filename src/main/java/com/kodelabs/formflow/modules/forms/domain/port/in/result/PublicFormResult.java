package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.FormType;

import java.util.List;
import java.util.UUID;

public record PublicFormResult(
        UUID formId,
        String name,
        FormType type,
        Integer timeLimitSeconds,
        String tenantName,
        String tenantLogoUrl,
        String tenantPrimaryColor,
        List<PublicSectionResult> sections
) {}
