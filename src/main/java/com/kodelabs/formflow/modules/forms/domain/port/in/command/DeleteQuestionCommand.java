package com.kodelabs.formflow.modules.forms.domain.port.in.command;

import java.util.UUID;

public record DeleteQuestionCommand(UUID questionId, UUID sectionId, UUID formId, UUID tenantId, UUID userId) {}
