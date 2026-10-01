package com.kodelabs.formflow.modules.auth.domain.port.in.command;

import java.util.UUID;

/** fileId/avatarUrl are pre-computed by the controller (URL-building is a web-layer
 *  concern, kept out of the application layer) — same split UploadLogoCommand uses. */
public record UploadAvatarCommand(
        UUID userId, UUID tenantId, UUID fileId, String avatarUrl, String originalFilename, byte[] content) {}
