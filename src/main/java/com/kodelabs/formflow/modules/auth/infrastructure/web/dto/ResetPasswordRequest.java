package com.kodelabs.formflow.modules.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

        @Schema(description = "Token recibido en el correo de recuperación")
        @NotBlank(message = "{validation.token.required}")
        String token,

        @Schema(description = "Nueva contraseña (mínimo 8 caracteres)", example = "nuevaClave123")
        @NotBlank(message = "{validation.password.required}")
        @Size(min = 8, max = 100, message = "{validation.password.size}")
        String newPassword
) {}
