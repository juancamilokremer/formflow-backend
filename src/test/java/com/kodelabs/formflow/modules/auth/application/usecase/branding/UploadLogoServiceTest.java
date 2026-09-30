package com.kodelabs.formflow.modules.auth.application.usecase.branding;

import com.kodelabs.formflow.modules.auth.application.service.BrandingCache;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UploadLogoCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.storage.FileStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadLogoServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private FileStoragePort fileStorage;
    @Mock private BrandingCache brandingCache;
    @InjectMocks private UploadLogoService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID fileId = UUID.randomUUID();
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = Tenant.builder().id(tenantId).slug("empresa-abc").build();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
    }

    @Test
    void storesTheLogoAndUpdatesTheTenant() throws IOException {
        when(tenantRepository.save(tenant)).thenReturn(tenant);
        byte[] png = pngOf(100, 50);
        var command = new UploadLogoCommand(tenantId, fileId, "http://x/logo.png", "logo.png", png);

        BrandingResult result = service.execute(command);

        assertThat(result.logoUrl()).isEqualTo("http://x/logo.png");
        assertThat(tenant.getLogoUrl()).isEqualTo("http://x/logo.png");
        verify(fileStorage).store(eq(fileId), eq("logo.png"), any());
        verify(brandingCache).invalidate("empresa-abc");
    }

    @Test
    void resizesAnOversizedPngDownTo400x200() throws IOException {
        when(tenantRepository.save(tenant)).thenReturn(tenant);
        byte[] bigPng = pngOf(1200, 600);
        var command = new UploadLogoCommand(tenantId, fileId, "http://x/logo.png", "logo.png", bigPng);

        service.execute(command);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(fileStorage).store(eq(fileId), eq("logo.png"), captor.capture());
        BufferedImage resized = ImageIO.read(new java.io.ByteArrayInputStream(captor.getValue()));
        assertThat(resized.getWidth()).isLessThanOrEqualTo(400);
        assertThat(resized.getHeight()).isLessThanOrEqualTo(200);
    }

    @Test
    void doesNotResizeSvg() throws IOException {
        when(tenantRepository.save(tenant)).thenReturn(tenant);
        byte[] svg = "<svg></svg>".getBytes();
        var command = new UploadLogoCommand(tenantId, fileId, "http://x/logo.svg", "logo.svg", svg);

        service.execute(command);

        verify(fileStorage).store(fileId, "logo.svg", svg);
    }

    @Test
    void rejectsALogoLargerThan2Mb() {
        byte[] tooLarge = new byte[3 * 1024 * 1024];
        var command = new UploadLogoCommand(tenantId, fileId, "http://x/logo.png", "logo.png", tooLarge);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.logo.too_large")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void rejectsAnExtensionNotInAllowedTypes() {
        var command = new UploadLogoCommand(tenantId, fileId, "http://x/logo.gif", "logo.gif", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.logo.type_not_allowed");
    }

    private byte[] pngOf(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
