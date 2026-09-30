package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request;

import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleRequest(
        @NotNull(message = "{validation.role.required}")
        UserRole role
) {}
