package com.kodelabs.formflow.modules.auth.application.usecase.me;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UploadAvatarCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.MeResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
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
class UploadAvatarServiceTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private FileStoragePort fileStorage;
    @InjectMocks private UploadAvatarService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID fileId = UUID.randomUUID();
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(userId).tenantId(tenantId).firstName("Ada").lastName("QA").build();
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
    }

    @Test
    void storesTheAvatarAndUpdatesTheUser() throws IOException {
        when(userRepository.save(user)).thenReturn(user);
        byte[] png = pngOf(100, 100);
        var command = new UploadAvatarCommand(userId, tenantId, fileId, "http://x/avatar.png", "avatar.png", png);

        MeResult result = service.execute(command);

        assertThat(result.avatarUrl()).isEqualTo("http://x/avatar.png");
        assertThat(user.getAvatarUrl()).isEqualTo("http://x/avatar.png");
        verify(fileStorage).store(eq(fileId), eq("avatar.png"), any());
    }

    @Test
    void resizesAnOversizedPngDownTo400x400() throws IOException {
        when(userRepository.save(user)).thenReturn(user);
        byte[] bigPng = pngOf(1200, 1200);
        var command = new UploadAvatarCommand(userId, tenantId, fileId, "http://x/avatar.png", "avatar.png", bigPng);

        service.execute(command);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(fileStorage).store(eq(fileId), eq("avatar.png"), captor.capture());
        BufferedImage resized = ImageIO.read(new java.io.ByteArrayInputStream(captor.getValue()));
        assertThat(resized.getWidth()).isLessThanOrEqualTo(400);
        assertThat(resized.getHeight()).isLessThanOrEqualTo(400);
    }

    @Test
    void rejectsAnAvatarLargerThan2Mb() {
        byte[] tooLarge = new byte[3 * 1024 * 1024];
        var command = new UploadAvatarCommand(userId, tenantId, fileId, "http://x/avatar.png", "avatar.png", tooLarge);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.avatar.too_large")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void rejectsAnExtensionNotInAllowedTypes() {
        var command = new UploadAvatarCommand(userId, tenantId, fileId, "http://x/avatar.svg", "avatar.svg", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.avatar.type_not_allowed");
    }

    @Test
    void rejectsWhenUserNotFound() {
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.empty());
        var command = new UploadAvatarCommand(userId, tenantId, fileId, "http://x/avatar.png", "avatar.png", new byte[]{1});

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.user.not_found");
    }

    private byte[] pngOf(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
