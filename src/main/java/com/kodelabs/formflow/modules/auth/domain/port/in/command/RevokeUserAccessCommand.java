package com.kodelabs.formflow.modules.auth.domain.port.in.command;

import java.util.UUID;

public record RevokeUserAccessCommand(UUID tenantId, UUID actingUserId, UUID targetUserId) {}
