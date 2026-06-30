package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record GetResponseDetailQuery(UUID formId, UUID responseId, UUID tenantId) {}
