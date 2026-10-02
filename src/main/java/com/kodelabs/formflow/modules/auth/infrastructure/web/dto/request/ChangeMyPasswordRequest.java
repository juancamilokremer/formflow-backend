package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeMyPasswordRequest(

        @NotBlank(message = "{validation.password.required}")
        String currentPassword,

        @NotBlank(message = "{validation.password.required}")
        @Size(min = 8, max = 100, message = "{validation.password.size}")
        String newPassword
) {}
