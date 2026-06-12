package com.kodelabs.formflow.modules.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @Schema(description = "Nombre visible de la empresa", example = "Empresa Demo S.A.S")
        @NotBlank(message = "El nombre de la empresa es obligatorio")
        @Size(max = 150, message = "El nombre de la empresa no puede superar 150 caracteres")
        String companyName,

        @Schema(description = "Identificador único URL-friendly de la empresa", example = "empresa-demo")
        @NotBlank(message = "El identificador (slug) es obligatorio")
        @Size(min = 3, max = 100, message = "El slug debe tener entre 3 y 100 caracteres")
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
                 message = "El slug solo puede contener minúsculas, números y guiones (ej: empresa-abc)")
        String slug,

        @Schema(description = "Email del usuario administrador", example = "admin@demo.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 150, message = "El email no puede superar 150 caracteres")
        String email,

        @Schema(description = "Contraseña del administrador (mínimo 8 caracteres)", example = "password123")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password,

        @Schema(description = "Nombre del administrador", example = "Juan")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String firstName,

        @Schema(description = "Apellido del administrador", example = "Kremer")
        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
        String lastName
) {}
