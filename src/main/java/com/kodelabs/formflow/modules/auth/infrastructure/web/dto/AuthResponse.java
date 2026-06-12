package com.kodelabs.formflow.modules.auth.infrastructure.web.dto;

import com.kodelabs.formflow.modules.auth.application.usecase.AuthResult;

/**
 * Response of the authentication endpoints.
 * Web-layer DTO — the domain model is never exposed directly.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInMs,
        UserSummary user,
        TenantSummary tenant
) {

    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(
                result.accessToken(),
                result.refreshToken(),
                "Bearer",
                result.expiresInMs(),
                UserSummary.from(result.user()),
                TenantSummary.from(result.tenant()));
    }
}
