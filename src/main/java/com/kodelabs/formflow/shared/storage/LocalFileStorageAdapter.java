package com.kodelabs.formflow.shared.storage;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

/**
 * Filesystem-backed FileStoragePort — the "no Redis, no S3 needed for the single-instance MVP"
 * choice, same reasoning CLAUDE.md already applies to the rate-limit cache. Each file lives
 * under {uploads-dir}/{fileId}/{filename}: the UUID directory is the opaque, unguessable lookup
 * key, so the filename itself never needs to be randomized or used for anything but display.
 */
@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private final Path uploadsDir;

    public LocalFileStorageAdapter(@Value("${app.uploads.dir}") String uploadsDir) {
        this.uploadsDir = Path.of(uploadsDir);
    }

    @Override
    @SneakyThrows
    public void store(UUID fileId, String filename, byte[] content) {
        Path dir = uploadsDir.resolve(fileId.toString());
        Files.createDirectories(dir);
        Files.write(dir.resolve(filename), content);
    }

    @Override
    public Optional<StoredFile> load(UUID fileId) {
        Path dir = uploadsDir.resolve(fileId.toString());
        if (!Files.isDirectory(dir)) return Optional.empty();

        try (var entries = Files.list(dir)) {
            return entries.findFirst().map(this::readFile);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @SneakyThrows
    private StoredFile readFile(Path file) {
        String contentType = MediaTypeFactory.getMediaType(file.getFileName().toString())
                .map(Object::toString)
                .orElse("application/octet-stream");
        return new StoredFile(file.getFileName().toString(), contentType, Files.readAllBytes(file));
    }
}
