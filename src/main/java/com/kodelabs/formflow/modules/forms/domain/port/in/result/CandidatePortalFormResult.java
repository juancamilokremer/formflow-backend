package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.UUID;

public record CandidatePortalFormResult(UUID formId, String name, boolean completed) {}
