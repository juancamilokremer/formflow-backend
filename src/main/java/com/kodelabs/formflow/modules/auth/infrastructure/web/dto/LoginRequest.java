package com.kodelabs.formflow.modules.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(description = "Slug de la empresa a la que pertenece el usuario", example = "empresa-demo")
        @NotBlank(message = "{validation.tenant_slug.required}")
        String tenantSlug,

        @Schema(description = "Email del usuario", example = "admin@demo.com")
        @NotBlank(message = "{validation.email.required}")
        @Email(message = "{validation.email.format}")
        String email,

        @Schema(description = "Contraseña del usuario", example = "password123")
        @NotBlank(message = "{validation.password.required}")
        String password
) {}
