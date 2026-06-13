package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.application.service.AuthEmailSender;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ForgotPasswordCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private AuthEmailSender authEmailSender;

    @InjectMocks
    private ForgotPasswordService service;

    @Test
    void sendsResetEmailWhenUserExists() {
        Tenant tenant = Tenant.builder().id(UUID.randomUUID()).slug("empresa-abc").name("ABC").build();
        User user = User.builder().id(UUID.randomUUID()).tenantId(tenant.getId())
                .email("admin@abc.com").firstName("Juan").lastName("K").build();
        when(tenantRepository.findBySlug("empresa-abc")).thenReturn(Optional.of(tenant));
        when(userRepository.findByEmailAndTenantId("admin@abc.com", tenant.getId()))
                .thenReturn(Optional.of(user));

        service.execute(new ForgotPasswordCommand("empresa-abc", "admin@abc.com"));

        verify(authEmailSender).sendPasswordReset(user, tenant);
    }

    @Test
    void completesSilentlyWhenEmailDoesNotExist_userEnumerationProtection() {
        Tenant tenant = Tenant.builder().id(UUID.randomUUID()).slug("empresa-abc").name("ABC").build();
        when(tenantRepository.findBySlug("empresa-abc")).thenReturn(Optional.of(tenant));
        when(userRepository.findByEmailAndTenantId(any(), any())).thenReturn(Optional.empty());

        assertThatCode(() -> service.execute(
                new ForgotPasswordCommand("empresa-abc", "noexiste@abc.com")))
                .doesNotThrowAnyException();

        verify(authEmailSender, never()).sendPasswordReset(any(), any());
    }

    @Test
    void completesSilentlyWhenTenantDoesNotExist() {
        when(tenantRepository.findBySlug("fantasma")).thenReturn(Optional.empty());

        assertThatCode(() -> service.execute(
                new ForgotPasswordCommand("fantasma", "alguien@x.com")))
                .doesNotThrowAnyException();

        verify(authEmailSender, never()).sendPasswordReset(any(), any());
    }
}
