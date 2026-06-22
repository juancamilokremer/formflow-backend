package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;

import java.util.UUID;

public record UpdateFormStatusCommand(UUID formId, UUID tenantId, UUID userId, FormStatus status) {}
