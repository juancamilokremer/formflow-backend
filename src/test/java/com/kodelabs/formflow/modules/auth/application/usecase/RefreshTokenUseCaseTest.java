package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.domain.model.GeneratedRefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.RefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private TokenServicePort tokenService;

    @InjectMocks
    private RefreshTokenUseCase useCase;

    private Tenant tenant;
    private User user;
    private RefreshToken storedToken;
    private final RefreshTokenCommand command = new RefreshTokenCommand("raw-token");

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
                .firstName("Juan")
                .lastName("Kremer")
                .role(UserRole.TENANT_ADMIN)
                .build();
        storedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tenantId(tenant.getId())
                .tokenHash("hash-token")
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(tokenService.hashRefreshToken("raw-token")).thenReturn("hash-token");
    }

    @Test
    void rotatesTokensRevokingTheUsedOneAndIssuingNewPair() {
        when(refreshTokenRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(storedToken));
        when(userRepository.findByIdAndTenantId(user.getId(), tenant.getId())).thenReturn(Optional.of(user));
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tokenService.generateAccessToken(user)).thenReturn("new-jwt");
        when(tokenService.generateRefreshToken()).thenReturn(
                new GeneratedRefreshToken("new-raw", "new-hash",
                        Instant.now().plusSeconds(3600)));
        when(tokenService.accessTokenValidityMs()).thenReturn(86400000L);

        AuthResult result = useCase.execute(command);

        assertThat(result.accessToken()).isEqualTo("new-jwt");
        assertThat(result.refreshToken()).isEqualTo("new-raw");

        // The used token was revoked and the new one persisted
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues())
                .anySatisfy(t -> {
                    assertThat(t.getTokenHash()).isEqualTo("hash-token");
                    assertThat(t.isRevoked()).isTrue();
                })
                .anySatisfy(t -> assertThat(t.getTokenHash()).isEqualTo("new-hash"));
    }

    @Test
    void failsWhenTokenDoesNotExist() {
        when(refreshTokenRepository.findByTokenHash("hash-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Refresh token inválido o expirado");
    }

    @Test
    void failsWhenTokenIsExpired() {
        storedToken.setExpiresAt(Instant.now().minusSeconds(10));
        when(refreshTokenRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Refresh token inválido o expirado");
    }

    @Test
    void revokedTokenReuseRevokesAllUserTokens() {
        storedToken.setRevokedAt(Instant.now().minusSeconds(60));
        when(refreshTokenRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Refresh token inválido o expirado");

        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
        verify(tokenService, never()).generateAccessToken(user);
    }

    @Test
    void failsWhenUserIsInactive() {
        user.setActive(false);
        when(refreshTokenRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(storedToken));
        when(userRepository.findByIdAndTenantId(user.getId(), tenant.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Refresh token inválido o expirado");
    }
}
