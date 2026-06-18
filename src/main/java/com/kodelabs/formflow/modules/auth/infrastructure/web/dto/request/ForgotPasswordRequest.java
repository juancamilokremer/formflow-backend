package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(

        @Schema(description = "Slug de la empresa", example = "empresa-demo")
        @NotBlank(message = "{validation.tenant_slug.required}")
        String tenantSlug,

        @Schema(description = "Email de la cuenta a recuperar", example = "admin@demo.com")
        @NotBlank(message = "{validation.email.required}")
        @Email(message = "{validation.email.format}")
        String email
) {}
