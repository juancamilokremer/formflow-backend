package com.kodelabs.formflow.modules.auth.infrastructure.web.dto;

import com.kodelabs.formflow.modules.auth.domain.port.in.result.RegisterTenantResult;

/**
 * Response of POST /auth/register.
 * No tokens — the admin must confirm their email before they can log in.
 */
public record RegisterResponse(
        UserSummary user,
        TenantSummary tenant
) {

    public static RegisterResponse from(RegisterTenantResult result) {
        return new RegisterResponse(
                UserSummary.from(result.user()),
                TenantSummary.from(result.tenant()));
    }
}
