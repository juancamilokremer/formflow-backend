package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.MeResult;

public record MeResponse(String firstName, String lastName, String email, UserRole role, String avatarUrl) {
    public static MeResponse from(MeResult r) {
        return new MeResponse(r.firstName(), r.lastName(), r.email(), r.role(), r.avatarUrl());
    }
}
