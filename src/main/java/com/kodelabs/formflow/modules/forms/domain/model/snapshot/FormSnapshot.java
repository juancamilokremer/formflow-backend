package com.kodelabs.formflow.modules.forms.domain.model.snapshot;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FormSnapshot(
        UUID formId,
        String formName,
        String formType,
        int formVersion,
        Instant capturedAt,
        List<SectionSnapshot> sections
) {}
