package com.kodelabs.formflow.modules.auth.domain.port.in.command;

import java.util.UUID;

/** Never touches logoUrl — that is only managed by UploadLogoUseCase/DeleteLogoUseCase. */
public record UpdateBrandingCommand(UUID tenantId, String name, String primaryColor, String secondaryColor) {}
