package com.kodelabs.formflow.modules.auth.application.usecase.me;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.UploadAvatarUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UploadAvatarCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.MeResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.storage.FileStoragePort;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/** Mirrors UploadLogoService (branding) — same size/type validation and resize shape, but a
 *  square box for a profile photo instead of a wordmark's landscape box, no SVG (a personal
 *  photo is never vector art), and no BrandingCache involved. */
@Service
@RequiredArgsConstructor
public class UploadAvatarService implements UploadAvatarUseCase {

    private static final int MAX_SIZE_MB = 2;
    private static final List<String> ALLOWED_TYPES = List.of("png", "jpg", "jpeg");
    private static final int MAX_SIZE_PX = 400;

    private final UserRepositoryPort userRepository;
    private final FileStoragePort fileStorage;

    @Override
    @Transactional
    public MeResult execute(UploadAvatarCommand command) {
        User user = userRepository.findByIdAndTenantId(command.userId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.user.not_found", HttpStatus.NOT_FOUND));

        validateSize(command.content().length);
        String filename = sanitizeFilename(command.originalFilename());
        String extension = extensionOf(filename);
        validateExtension(extension);

        byte[] resized = resize(command.content(), extension);
        fileStorage.store(command.fileId(), filename, resized);

        user.setAvatarUrl(command.avatarUrl());
        return MeResult.from(userRepository.save(user));
    }

    /** Thumbnailator never enlarges a source already smaller than the target box — a single
     *  call covers "resize only if it exceeds 400x400". */
    private byte[] resize(byte[] content, String extension) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(content))
                    .size(MAX_SIZE_PX, MAX_SIZE_PX)
                    .outputFormat(outputFormatFor(extension))
                    .toOutputStream(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("error.avatar.type_not_allowed", HttpStatus.BAD_REQUEST, extension, ALLOWED_TYPES);
        }
    }

    private String outputFormatFor(String extension) {
        return "jpg".equals(extension) ? "jpeg" : extension;
    }

    private void validateSize(int contentLength) {
        long maxBytes = MAX_SIZE_MB * 1024L * 1024L;
        if (contentLength > maxBytes) {
            throw new BusinessException("error.avatar.too_large", HttpStatus.BAD_REQUEST, MAX_SIZE_MB);
        }
    }

    private void validateExtension(String extension) {
        if (ALLOWED_TYPES.stream().noneMatch(t -> t.equalsIgnoreCase(extension))) {
            throw new BusinessException("error.avatar.type_not_allowed", HttpStatus.BAD_REQUEST, extension, ALLOWED_TYPES);
        }
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
    }

    /** Same leaf-only sanitation as UploadLogoService/UploadAnswerFileService (#158). */
    private String sanitizeFilename(String originalFilename) {
        String leaf = originalFilename.replace('\\', '/');
        int lastSlash = leaf.lastIndexOf('/');
        leaf = lastSlash >= 0 ? leaf.substring(lastSlash + 1) : leaf;
        return leaf.isBlank() ? "avatar" : leaf;
    }
}
