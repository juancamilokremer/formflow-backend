package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record GetFormQuery(UUID formId, UUID tenantId) {}
