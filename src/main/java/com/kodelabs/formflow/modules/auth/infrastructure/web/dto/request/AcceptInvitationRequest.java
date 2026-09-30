package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInvitationRequest(

        @NotBlank(message = "{validation.first_name.required}")
        @Size(max = 100, message = "{validation.first_name.size}")
        String firstName,

        @NotBlank(message = "{validation.last_name.required}")
        @Size(max = 100, message = "{validation.last_name.size}")
        String lastName,

        @NotBlank(message = "{validation.password.required}")
        @Size(min = 8, max = 100, message = "{validation.password.size}")
        String password
) {}
