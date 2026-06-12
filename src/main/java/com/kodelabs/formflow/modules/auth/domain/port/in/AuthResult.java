package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;

/**
 * Common result of the authentication input ports:
 * token pair + basic data of the user and its tenant.
 */
public record AuthResult(
        String accessToken,
        String refreshToken,
        long expiresInMs,
        User user,
        Tenant tenant
) {}
