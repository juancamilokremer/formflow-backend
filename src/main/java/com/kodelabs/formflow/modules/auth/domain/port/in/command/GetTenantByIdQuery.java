package com.kodelabs.formflow.modules.auth.domain.port.in.command;

import java.util.UUID;

/** Super-admin lookup: no scoping to the caller's own tenant. */
public record GetTenantByIdQuery(UUID tenantId) {}
