package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.InvitationPreviewResult;

public record InvitationPreviewResponse(String tenantName, String email, UserRole role) {
    public static InvitationPreviewResponse from(InvitationPreviewResult r) {
        return new InvitationPreviewResponse(r.tenantName(), r.email(), r.role());
    }
}
