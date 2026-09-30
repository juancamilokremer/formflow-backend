package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.auth.domain.port.in.result.AcceptInvitationResult;

import java.util.UUID;

public record AcceptInvitationResponse(UUID userId, String email) {
    public static AcceptInvitationResponse from(AcceptInvitationResult r) {
        return new AcceptInvitationResponse(r.userId(), r.email());
    }
}
