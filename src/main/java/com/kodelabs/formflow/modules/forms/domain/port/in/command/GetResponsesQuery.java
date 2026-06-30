package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record GetResponsesQuery(UUID formId, UUID tenantId, int page, int size) {}
