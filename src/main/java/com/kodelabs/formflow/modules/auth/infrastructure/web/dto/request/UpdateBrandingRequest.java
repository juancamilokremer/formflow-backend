package com.kodelabs.formflow.modules.auth.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateBrandingRequest(

        @NotBlank(message = "{validation.branding_name.required}")
        @Size(max = 150, message = "{validation.company_name.size}")
        String name,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "{validation.color.hex}")
        String primaryColor,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "{validation.color.hex}")
        String secondaryColor
) {}
