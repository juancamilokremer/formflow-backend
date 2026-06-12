package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.domain.model.GeneratedRefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.RefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterTenantUseCaseTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock private PasswordHasherPort passwordHasher;
    @Mock private TokenServicePort tokenService;

    @InjectMocks
    private RegisterTenantUseCase useCase;

    private RegisterTenantCommand command;

    @BeforeEach
    void setUp() {
        command = new RegisterTenantCommand(
                "Empresa ABC", "empresa-abc", "admin@abc.com",
                "password123", "Juan", "Kremer");
    }

    @Test
    void registersTenantAndAdminWithTokens() {
        UUID tenantId = UUID.randomUUID();
        when(tenantRepository.existsBySlug("empresa-abc")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> {
            Tenant t = inv.getArgument(0);
            t.setId(tenantId);
            return t;
        });
        when(passwordHasher.hash("password123")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(tokenService.generateAccessToken(any(User.class))).thenReturn("jwt-access");
        when(tokenService.generateRefreshToken()).thenReturn(
                new GeneratedRefreshToken("raw-refresh", "hash-refresh",
                        Instant.now().plusSeconds(3600)));
        when(tokenService.accessTokenValidityMs()).thenReturn(86400000L);

        AuthResult result = useCase.execute(command);

        assertThat(result.accessToken()).isEqualTo("jwt-access");
        assertThat(result.refreshToken()).isEqualTo("raw-refresh");
        assertThat(result.tenant().getSlug()).isEqualTo("empresa-abc");
        assertThat(result.user().getRole()).isEqualTo(UserRole.TENANT_ADMIN);

        // The password is persisted hashed, never in plain text
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("$2a$hashed");
        assertThat(userCaptor.getValue().getTenantId()).isEqualTo(tenantId);

        // The refresh token is persisted by hash, not by raw value
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getTokenHash()).isEqualTo("hash-refresh");
    }

    @Test
    void failsWithConflictWhenSlugAlreadyExists() {
        when(tenantRepository.existsBySlug("empresa-abc")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
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
        when(tokenService.generateAccessToken(any())).thenReturn("jwt");
        when(tokenService.generateRefreshToken()).thenReturn(
                new GeneratedRefreshToken("raw", "hash", Instant.now().plusSeconds(60)));

        var uppercased = new RegisterTenantCommand(
                "Empresa ABC", "empresa-abc", "  Admin@ABC.com ",
                "password123", "Juan", "Kremer");

        AuthResult result = useCase.execute(uppercased);

        assertThat(result.user().getEmail()).isEqualTo("admin@abc.com");
    }
}
