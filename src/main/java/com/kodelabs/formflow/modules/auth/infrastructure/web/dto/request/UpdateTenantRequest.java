package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTenantRequest(

        @Schema(description = "Nombre visible de la empresa", example = "Empresa Demo S.A.S")
        @NotBlank(message = "{validation.company_name.required}")
        @Size(max = 150, message = "{validation.company_name.size}")
        String name,

        @Schema(description = "URL del logo de la empresa")
        String logoUrl,

        @Schema(description = "Color primario en hex", example = "#3B82F6")
        String primaryColor,

        @Schema(description = "Color secundario en hex", example = "#1E293B")
        String secondaryColor,

        @Schema(description = "Presente solo para detectar y rechazar un intento de cambiarlo — es inmutable")
        String slug
) {}
