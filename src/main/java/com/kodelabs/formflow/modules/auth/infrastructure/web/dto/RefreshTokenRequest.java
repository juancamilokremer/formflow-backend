package com.kodelabs.formflow.modules.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @Schema(description = "Refresh token recibido en el login o en la última rotación",
                example = "NhvnVsj-VR8tRPrFhmmrYmrEjrvM92EJlhQGng-q8p0")
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken
) {}
