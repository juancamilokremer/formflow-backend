package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.domain.model.EmailToken;
import com.kodelabs.formflow.modules.auth.domain.model.EmailTokenType;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ResetPasswordCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.EmailTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.RefreshTokenRepositoryPort;
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
class ResetPasswordServiceTest {

    @Mock private EmailTokenRepositoryPort emailTokenRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock private PasswordHasherPort passwordHasher;
    @Mock private TokenServicePort tokenService;

    @InjectMocks
    private ResetPasswordService service;

    private User user;
    private EmailToken token;
    private final ResetPasswordCommand command = new ResetPasswordCommand("raw-token", "nuevaClave123");

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .email("admin@abc.com")
                .passwordHash("$2a$old")
                .firstName("Juan")
                .lastName("K")
                .build();
        token = EmailToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .tokenHash("hash-token")
                .type(EmailTokenType.PASSWORD_RESET)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(tokenService.hashToken("raw-token")).thenReturn("hash-token");
    }

    @Test
    void resetsPasswordMarksTokenUsedAndRevokesAllRefreshTokens() {
        when(emailTokenRepository.findByTokenHashAndType("hash-token", EmailTokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));
        when(userRepository.findByIdAndTenantId(user.getId(), user.getTenantId()))
                .thenReturn(Optional.of(user));
        when(passwordHasher.hash("nuevaClave123")).thenReturn("$2a$new");

        service.execute(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("$2a$new");

        ArgumentCaptor<EmailToken> tokenCaptor = ArgumentCaptor.forClass(EmailToken.class);
        verify(emailTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().isUsed()).isTrue();

        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
    }

    @Test
    void failsWhenTokenDoesNotExist() {
        when(emailTokenRepository.findByTokenHashAndType("hash-token", EmailTokenType.PASSWORD_RESET))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.auth.email_token_invalid");

        verify(userRepository, never()).save(any());
        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }

    @Test
    void failsWhenTokenIsExpired() {
        token.setExpiresAt(Instant.now().minusSeconds(10));
        when(emailTokenRepository.findByTokenHashAndType("hash-token", EmailTokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.auth.email_token_invalid");
    }

    @Test
    void failsWhenTokenWasAlreadyUsed_singleUse() {
        token.setUsedAt(Instant.now().minusSeconds(60));
        when(emailTokenRepository.findByTokenHashAndType("hash-token", EmailTokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.auth.email_token_invalid");

        verify(userRepository, never()).save(any());
    }
}
