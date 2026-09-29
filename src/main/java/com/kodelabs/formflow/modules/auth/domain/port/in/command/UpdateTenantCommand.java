package com.kodelabs.formflow.modules.auth.domain.port.in.command;

import java.util.UUID;

/** slug is only carried to detect (and reject) an attempt to change it — see #5 acceptance criteria. */
public record UpdateTenantCommand(
        UUID tenantId,
        String name,
        String logoUrl,
        String primaryColor,
        String secondaryColor,
        String slug
) {}
