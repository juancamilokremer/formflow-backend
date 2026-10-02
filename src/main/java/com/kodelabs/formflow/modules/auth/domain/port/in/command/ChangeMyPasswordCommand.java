package com.kodelabs.formflow.modules.auth.domain.port.in.command;

import java.util.UUID;

public record ChangeMyPasswordCommand(UUID userId, UUID tenantId, String currentPassword, String newPassword) {}
