package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.application.service.TokenIssuer;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.AuthResult;
import com.kodelabs.formflow.modules.auth.domain.port.in.RegisterTenantCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterTenantServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private PasswordHasherPort passwordHasher;
    @Mock private TokenIssuer tokenIssuer;

    @InjectMocks
    private RegisterTenantService service;

    private RegisterTenantCommand command;

    @BeforeEach
    void setUp() {
        command = new RegisterTenantCommand(
                "Empresa ABC", "empresa-abc", "admin@abc.com",
                "password123", "Juan", "Kremer");
    }

    @Test
    void registersTenantAndAdminAndDelegatesTokenIssuing() {
        UUID tenantId = UUID.randomUUID();
        when(tenantRepository.existsBySlug("empresa-abc")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> {
            Tenant t = inv.getArgument(0);
            t.setId(tenantId);
            return t;
        });
        when(passwordHasher.hash("password123")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        AuthResult issued = new AuthResult("jwt", "refresh", 86400000L, null, null);
        when(tokenIssuer.issueFor(any(User.class), any(Tenant.class))).thenReturn(issued);

        AuthResult result = service.execute(command);

        assertThat(result).isSameAs(issued);

        // The admin user is created with the hashed password, never plain text
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User admin = userCaptor.getValue();
        assertThat(admin.getPasswordHash()).isEqualTo("$2a$hashed");
        assertThat(admin.getTenantId()).isEqualTo(tenantId);
        assertThat(admin.getRole()).isEqualTo(UserRole.TENANT_ADMIN);
    }

    @Test
    void failsWithConflictWhenSlugAlreadyExists() {
        when(tenantRepository.existsBySlug("empresa-abc")).thenReturn(true);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(tenantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void normalizesEmailToLowercase() {
        when(tenantRepository.existsBySlug(anyString())).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordHasher.hash(anyString())).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenIssuer.issueFor(any(), any())).thenReturn(
                new AuthResult("jwt", "refresh", 1L, null, null));

        var uppercasedEmail = new RegisterTenantCommand(
                "Empresa ABC", "empresa-abc", "  Admin@ABC.com ",
                "password123", "Juan", "Kremer");

        service.execute(uppercasedEmail);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("admin@abc.com");
    }
}
