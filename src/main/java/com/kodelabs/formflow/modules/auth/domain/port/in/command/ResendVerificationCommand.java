package com.kodelabs.formflow.modules.auth.domain.port.in.command;

import java.util.UUID;

/**
 * Input of ResendVerificationUseCase — built from the authenticated principal.
 */
public record ResendVerificationCommand(UUID userId, UUID tenantId) {}
