package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request;

import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InviteUserRequest(

        @NotBlank(message = "{validation.email.required}")
        @Email(message = "{validation.email.format}")
        @Size(max = 150, message = "{validation.email.size}")
        String email,

        @NotNull(message = "{validation.role.required}")
        UserRole role
) {}
