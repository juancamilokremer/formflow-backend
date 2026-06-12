package com.kodelabs.formflow.modules.auth.application.service;

import com.kodelabs.formflow.modules.auth.domain.model.GeneratedRefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.RefreshToken;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.AuthResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.RefreshTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenIssuerTest {

    @Mock private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock private TokenServicePort tokenService;

    @InjectMocks
    private TokenIssuer tokenIssuer;

    @Test
    void issuesTokenPairPersistingOnlyTheRefreshTokenHash() {
        Tenant tenant = Tenant.builder()
                .id(UUID.randomUUID())
                .slug("empresa-abc")
                .name("Empresa ABC")
                .build();
        User user = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenant.getId())
                .email("admin@abc.com")
                .firstName("Juan")
                .lastName("Kremer")
                .role(UserRole.TENANT_ADMIN)
                .build();
        Instant expiresAt = Instant.now().plusSeconds(3600);

        when(tokenService.generateAccessToken(user)).thenReturn("jwt-access");
        when(tokenService.generateRefreshToken()).thenReturn(
                new GeneratedRefreshToken("raw-refresh", "hash-refresh", expiresAt));
        when(tokenService.accessTokenValidityMs()).thenReturn(86400000L);

        AuthResult result = tokenIssuer.issueFor(user, tenant);

        assertThat(result.accessToken()).isEqualTo("jwt-access");
        assertThat(result.refreshToken()).isEqualTo("raw-refresh");
        assertThat(result.expiresInMs()).isEqualTo(86400000L);
        assertThat(result.user()).isEqualTo(user);
        assertThat(result.tenant()).isEqualTo(tenant);

        // Only the hash reaches the repository, never the raw value
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();
        assertThat(saved.getTokenHash()).isEqualTo("hash-refresh");
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getTenantId()).isEqualTo(tenant.getId());
        assertThat(saved.getExpiresAt()).isEqualTo(expiresAt);
    }
}
