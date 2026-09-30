package com.kodelabs.formflow.modules.auth.application.usecase.branding;

import com.kodelabs.formflow.modules.auth.application.service.BrandingCache;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.UploadLogoUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UploadLogoCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
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

@Service
@RequiredArgsConstructor
public class UploadLogoService implements UploadLogoUseCase {

    private static final int MAX_SIZE_MB = 2;
    private static final List<String> ALLOWED_TYPES = List.of("png", "jpg", "jpeg", "svg");
    private static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 200;

    private final TenantRepositoryPort tenantRepository;
    private final FileStoragePort fileStorage;
    private final BrandingCache brandingCache;

    @Override
    @Transactional
    public BrandingResult execute(UploadLogoCommand command) {
        Tenant tenant = tenantRepository.findById(command.tenantId())
                .orElseThrow(() -> new BusinessException("error.tenant.not_found"));

        validateSize(command.content().length);
        String filename = sanitizeFilename(command.originalFilename());
        String extension = extensionOf(filename);
        validateExtension(extension);

        byte[] processed = "svg".equals(extension) ? command.content() : resize(command.content(), extension);
        fileStorage.store(command.fileId(), filename, processed);

        tenant.setLogoUrl(command.logoUrl());
        Tenant saved = tenantRepository.save(tenant);

        brandingCache.invalidate(saved.getSlug());
        return BrandingResult.from(saved);
    }

    /** Thumbnailator never enlarges a source already smaller than the target box — a single
     *  call covers "resize only if it exceeds 400x200". */
    private byte[] resize(byte[] content, String extension) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(content))
                    .size(MAX_WIDTH, MAX_HEIGHT)
                    .outputFormat(outputFormatFor(extension))
                    .toOutputStream(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("error.logo.type_not_allowed", HttpStatus.BAD_REQUEST, extension, ALLOWED_TYPES);
        }
    }

    private String outputFormatFor(String extension) {
        return "jpg".equals(extension) ? "jpeg" : extension;
    }

    private void validateSize(int contentLength) {
        long maxBytes = MAX_SIZE_MB * 1024L * 1024L;
        if (contentLength > maxBytes) {
            throw new BusinessException("error.logo.too_large", HttpStatus.BAD_REQUEST, MAX_SIZE_MB);
        }
    }

    private void validateExtension(String extension) {
        if (ALLOWED_TYPES.stream().noneMatch(t -> t.equalsIgnoreCase(extension))) {
            throw new BusinessException("error.logo.type_not_allowed", HttpStatus.BAD_REQUEST, extension, ALLOWED_TYPES);
        }
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
    }

    /** Same leaf-only sanitation as UploadAnswerFileService (#158) — strips path segments a
     *  malicious client could send, since this is used as-is as the stored file's name. */
    private String sanitizeFilename(String originalFilename) {
        String leaf = originalFilename.replace('\\', '/');
        int lastSlash = leaf.lastIndexOf('/');
        leaf = lastSlash >= 0 ? leaf.substring(lastSlash + 1) : leaf;
        return leaf.isBlank() ? "logo" : leaf;
    }
}
