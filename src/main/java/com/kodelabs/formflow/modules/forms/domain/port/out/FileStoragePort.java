package com.kodelabs.formflow.modules.forms.domain.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Storage for files uploaded as answers to 'file'-type questions. LocalFileStorageAdapter is
 * the current implementation (filesystem, single-instance MVP); swapping to Cloudflare R2 later
 * only means adding a new adapter here, the use case and controller never change.
 */
public interface FileStoragePort {

    void store(UUID fileId, String filename, byte[] content);

    Optional<StoredFile> load(UUID fileId);
}
