package com.kodelabs.formflow.modules.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @Schema(description = "Nombre visible de la empresa", example = "Empresa Demo S.A.S")
        @NotBlank(message = "{validation.company_name.required}")
        @Size(max = 150, message = "{validation.company_name.size}")
        String companyName,

        @Schema(description = "Identificador único URL-friendly de la empresa", example = "empresa-demo")
        @NotBlank(message = "{validation.slug.required}")
        @Size(min = 3, max = 100, message = "{validation.slug.size}")
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "{validation.slug.pattern}")
        String slug,

        @Schema(description = "Email del usuario administrador", example = "admin@demo.com")
        @NotBlank(message = "{validation.email.required}")
        @Email(message = "{validation.email.format}")
        @Size(max = 150, message = "{validation.email.size}")
        String email,

        @Schema(description = "Contraseña del administrador (mínimo 8 caracteres)", example = "password123")
        @NotBlank(message = "{validation.password.required}")
        @Size(min = 8, max = 100, message = "{validation.password.size}")
        String password,

        @Schema(description = "Nombre del administrador", example = "Juan")
        @NotBlank(message = "{validation.first_name.required}")
        @Size(max = 100, message = "{validation.first_name.size}")
        String firstName,

        @Schema(description = "Apellido del administrador", example = "Kremer")
        @NotBlank(message = "{validation.last_name.required}")
        @Size(max = 100, message = "{validation.last_name.size}")
        String lastName
) {}
