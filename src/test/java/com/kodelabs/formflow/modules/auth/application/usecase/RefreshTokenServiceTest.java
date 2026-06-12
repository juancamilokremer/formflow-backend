package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.application.service.TokenIssuer;
import com.kodelabs.formflow.modules.auth.domain.model.RefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.AuthResult;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.RefreshTokenCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.RefreshTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private TokenServicePort tokenService;
    @Mock private TokenIssuer tokenIssuer;

    @InjectMocks
    private RefreshTokenService service;

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
    void rotatesTokenRevokingTheUsedOneBeforeIssuingNewPair() {
        when(refreshTokenRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(storedToken));
        when(userRepository.findByIdAndTenantId(user.getId(), tenant.getId())).thenReturn(Optional.of(user));
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        AuthResult issued = new AuthResult("new-jwt", "new-raw", 86400000L, user, tenant);
        when(tokenIssuer.issueFor(user, tenant)).thenReturn(issued);

        AuthResult result = service.execute(command);

        assertThat(result).isSameAs(issued);

        // The used token was persisted as revoked (single-use rotation)
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).isEqualTo("hash-token");
        assertThat(captor.getValue().isRevoked()).isTrue();
    }

    @Test
    void failsWhenTokenDoesNotExist() {
        when(refreshTokenRepository.findByTokenHash("hash-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.auth.refresh_token_invalid");
    }

    @Test
    void failsWhenTokenIsExpired() {
        storedToken.setExpiresAt(Instant.now().minusSeconds(10));
        when(refreshTokenRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.auth.refresh_token_invalid");
    }

    @Test
    void revokedTokenReuseRevokesAllUserTokens() {
        storedToken.setRevokedAt(Instant.now().minusSeconds(60));
        when(refreshTokenRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.auth.refresh_token_invalid");

        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
        verify(tokenIssuer, never()).issueFor(any(), any());
    }

    @Test
    void failsWhenUserIsInactive() {
        user.setActive(false);
        when(refreshTokenRepository.findByTokenHash("hash-token")).thenReturn(Optional.of(storedToken));
        when(userRepository.findByIdAndTenantId(user.getId(), tenant.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.auth.refresh_token_invalid");
    }
}
