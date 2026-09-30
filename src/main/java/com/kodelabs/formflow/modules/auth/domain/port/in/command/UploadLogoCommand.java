package com.kodelabs.formflow.modules.auth.domain.port.in.command;

import java.util.UUID;

/** fileId/logoUrl are pre-computed by the controller (URL-building is a web-layer concern,
 *  kept out of the application layer) — the use case just stores under the given id and
 *  persists the given URL onto the tenant. */
public record UploadLogoCommand(UUID tenantId, UUID fileId, String logoUrl, String originalFilename, byte[] content) {}
