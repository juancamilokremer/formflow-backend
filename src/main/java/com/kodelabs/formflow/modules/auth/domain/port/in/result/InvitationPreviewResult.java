package com.kodelabs.formflow.modules.auth.domain.port.in.result;

import com.kodelabs.formflow.modules.auth.domain.model.UserRole;

/** What the "accept invite" frontend page shows before asking for a password —
 *  deliberately minimal, no tenant internals beyond its display name. */
public record InvitationPreviewResult(String tenantName, String email, UserRole role) {}
