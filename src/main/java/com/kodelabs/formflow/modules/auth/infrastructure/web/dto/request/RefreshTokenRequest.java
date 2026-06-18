package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @Schema(description = "Refresh token recibido en el login o en la última rotación",
                example = "NhvnVsj-VR8tRPrFhmmrYmrEjrvM92EJlhQGng-q8p0")
        @NotBlank(message = "{validation.refresh_token.required}")
        String refreshToken
) {}
