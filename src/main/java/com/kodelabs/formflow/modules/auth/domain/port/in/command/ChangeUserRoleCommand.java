package com.kodelabs.formflow.modules.auth.domain.port.in.command;

import com.kodelabs.formflow.modules.auth.domain.model.UserRole;

import java.util.UUID;

public record ChangeUserRoleCommand(UUID tenantId, UUID targetUserId, UserRole newRole) {}
