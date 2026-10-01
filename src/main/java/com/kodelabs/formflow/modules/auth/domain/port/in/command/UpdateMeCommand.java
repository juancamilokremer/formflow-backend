package com.kodelabs.formflow.modules.auth.domain.port.in.command;

import java.util.UUID;

/** Never touches email — changing it would need re-verification, out of scope (#177). */
public record UpdateMeCommand(UUID userId, UUID tenantId, String firstName, String lastName) {}
