package com.kodelabs.formflow.modules.auth.domain.port.in.result;

import com.kodelabs.formflow.modules.auth.domain.model.UserRole;

/** What the "accept invite" frontend page shows before asking for a password —
 *  deliberately minimal, no tenant internals beyond its display name and slug (the slug
 *  is already public in every login/form URL; it's needed here so the frontend can
 *  pre-fill the tenant on the login form it redirects to after accepting, #170). */
public record InvitationPreviewResult(String tenantName, String tenantSlug, String email, UserRole role) {}
