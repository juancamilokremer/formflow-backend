package com.kodelabs.formflow.modules.auth.application.usecase.me;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ChangeMyPasswordCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeMyPasswordServiceTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private PasswordHasherPort passwordHasher;
    @InjectMocks private ChangeMyPasswordService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void changesThePasswordWhenCurrentPasswordMatches() {
        User user = User.builder().id(userId).tenantId(tenantId).passwordHash("old-hash").build();
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(passwordHasher.matches("current123", "old-hash")).thenReturn(true);
        when(passwordHasher.hash("newPassword1!")).thenReturn("new-hash");

        service.execute(new ChangeMyPasswordCommand(userId, tenantId, "current123", "newPassword1!"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("new-hash");
    }

    @Test
    void rejectsWhenCurrentPasswordDoesNotMatch() {
        User user = User.builder().id(userId).tenantId(tenantId).passwordHash("old-hash").build();
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.execute(new ChangeMyPasswordCommand(userId, tenantId, "wrong", "newPassword1!")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.auth.invalid_current_password")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsWhenUserNotFound() {
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new ChangeMyPasswordCommand(userId, tenantId, "current123", "newPassword1!")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.user.not_found");
    }
}
