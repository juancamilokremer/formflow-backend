package com.kodelabs.formflow.modules.auth.domain.port.in.result;

import java.util.UUID;

/** Minimal — the frontend redirects to /login after accepting, it does not need a session. */
public record AcceptInvitationResult(UUID userId, String email) {}
