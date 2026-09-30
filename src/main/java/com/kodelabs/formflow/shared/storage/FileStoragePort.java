package com.kodelabs.formflow.shared.storage;

import java.util.Optional;
import java.util.UUID;

/**
 * Generic file storage — used wherever a module needs to persist and later serve back an
 * uploaded file (answers to 'file'-type questions, tenant branding logos, etc). Lives in
 * shared/ rather than a single module's port/out/, the same way BusinessException or
 * ApiResponse do, since every module is expected to depend on it directly.
 *
 * LocalFileStorageAdapter is the current implementation (filesystem, single-instance MVP);
 * swapping to Cloudflare R2 later only means adding a new adapter here, callers never change.
 */
public interface FileStoragePort {

    void store(UUID fileId, String filename, byte[] content);

    Optional<StoredFile> load(UUID fileId);

    void delete(UUID fileId);
}
