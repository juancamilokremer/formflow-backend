package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.domain.model.GeneratedRefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantStatus;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.RefreshTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock private PasswordHasherPort passwordHasher;
    @Mock private TokenServicePort tokenService;

    @InjectMocks
    private LoginUseCase useCase;

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
    void authenticatesAndReturnsTokens() {
        when(tenantRepository.findBySlug("empresa-abc")).thenReturn(Optional.of(tenant));
        when(userRepository.findByEmailAndTenantId("admin@abc.com", tenant.getId()))
                .thenReturn(Optional.of(user));
        when(passwordHasher.matches("password123", "$2a$hashed")).thenReturn(true);
        when(tokenService.generateAccessToken(user)).thenReturn("jwt-access");
        when(tokenService.generateRefreshToken()).thenReturn(
                new GeneratedRefreshToken("raw-refresh", "hash-refresh",
                        Instant.now().plusSeconds(3600)));
        when(tokenService.accessTokenValidityMs()).thenReturn(86400000L);

        AuthResult result = useCase.execute(command);

        assertThat(result.accessToken()).isEqualTo("jwt-access");
        assertThat(result.refreshToken()).isEqualTo("raw-refresh");
        assertThat(result.user().getId()).isEqualTo(user.getId());
    }

    @Test
    void failsWithGenericMessageWhenTenantDoesNotExist() {
        when(tenantRepository.findBySlug("empresa-abc")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    void failsWithGenericMessageWhenUserDoesNotExist() {
        when(tenantRepository.findBySlug("empresa-abc")).thenReturn(Optional.of(tenant));
        when(userRepository.findByEmailAndTenantId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    void failsWithGenericMessageWhenPasswordIsWrong() {
        when(tenantRepository.findBySlug("empresa-abc")).thenReturn(Optional.of(tenant));
        when(userRepository.findByEmailAndTenantId("admin@abc.com", tenant.getId()))
                .thenReturn(Optional.of(user));
        when(passwordHasher.matches("password123", "$2a$hashed")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciales inválidas")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void failsWhenTenantIsSuspended() {
        tenant.setStatus(TenantStatus.SUSPENDED);
        when(tenantRepository.findBySlug("empresa-abc")).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> useCase.execute(command))
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

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciales inválidas");
    }
}
