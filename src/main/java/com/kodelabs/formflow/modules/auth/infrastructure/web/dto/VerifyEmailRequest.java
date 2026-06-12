package com.kodelabs.formflow.modules.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(

        @Schema(description = "Token recibido en el correo de verificación")
        @NotBlank(message = "{validation.token.required}")
        String token
) {}
