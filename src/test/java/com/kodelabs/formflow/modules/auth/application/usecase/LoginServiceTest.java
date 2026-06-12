package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.application.service.TokenIssuer;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantStatus;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.AuthResult;
import com.kodelabs.formflow.modules.auth.domain.port.in.LoginCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private PasswordHasherPort passwordHasher;
    @Mock private TokenIssuer tokenIssuer;

    @InjectMocks
    private LoginService service;

    private Tenant tenant;
    private User user;
    private LoginCommand command;

    @BeforeEach
    void setUp() {
        tenant = Tenant.builder()
                .id(UUID.randomUUID())
                .slug("empresa-abc")
                .name("Empresa ABC")
                .build();
        user = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenant.getId())
                .email("admin@abc.com")
                .passwordHash("$2a$hashed")
                .firstName("Juan")
                .lastName("Kremer")
                .role(UserRole.TENANT_ADMIN)
                .build();
        command = new LoginCommand("empresa-abc", "admin@abc.com", "password123");
    }

    @Test
    void authenticatesAndDelegatesTokenIssuing() {
        when(tenantRepository.findBySlug("empresa-abc")).thenReturn(Optional.of(tenant));
        when(userRepository.findByEmailAndTenantId("admin@abc.com", tenant.getId()))
                .thenReturn(Optional.of(user));
        when(passwordHasher.matches("password123", "$2a$hashed")).thenReturn(true);
        AuthResult issued = new AuthResult("jwt", "refresh", 86400000L, user, tenant);
        when(tokenIssuer.issueFor(user, tenant)).thenReturn(issued);

        AuthResult result = service.execute(command);

        assertThat(result).isSameAs(issued);
    }

    @Test
    void failsWithGenericMessageWhenTenantDoesNotExist() {
        when(tenantRepository.findBySlug("empresa-abc")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    void failsWithGenericMessageWhenUserDoesNotExist() {
        when(tenantRepository.findBySlug("empresa-abc")).thenReturn(Optional.of(tenant));
        when(userRepository.findByEmailAndTenantId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    void failsWithGenericMessageWhenPasswordIsWrong() {
        when(tenantRepository.findBySlug("empresa-abc")).thenReturn(Optional.of(tenant));
        when(userRepository.findByEmailAndTenantId("admin@abc.com", tenant.getId()))
                .thenReturn(Optional.of(user));
        when(passwordHasher.matches("password123", "$2a$hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciales inválidas")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));

        verify(tokenIssuer, never()).issueFor(any(), any());
    }

    @Test
    void failsWhenTenantIsSuspended() {
        tenant.setStatus(TenantStatus.SUSPENDED);
        when(tenantRepository.findBySlug("empresa-abc")).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void failsWhenUserIsInactive() {
        user.setActive(false);
        when(tenantRepository.findBySlug("empresa-abc")).thenReturn(Optional.of(tenant));
        when(userRepository.findByEmailAndTenantId("admin@abc.com", tenant.getId()))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciales inválidas");
    }
}
