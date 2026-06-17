package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.application.service.AuthEmailSender;
import com.kodelabs.formflow.modules.auth.domain.model.EmailToken;
import com.kodelabs.formflow.modules.auth.domain.model.EmailTokenType;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.VerifyEmailCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.EmailTokenRepositoryPort;
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
class VerifyEmailServiceTest {

    @Mock private EmailTokenRepositoryPort emailTokenRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private TokenServicePort tokenService;
    @Mock private AuthEmailSender authEmailSender;

    @InjectMocks
    private VerifyEmailService service;

    private User user;
    private Tenant tenant;
    private EmailToken token;
    private final VerifyEmailCommand command = new VerifyEmailCommand("raw-token");

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .email("admin@abc.com")
                .firstName("Juan")
                .lastName("K")
                .build();
        tenant = Tenant.builder()
                .id(user.getTenantId())
                .slug("empresa-abc")
                .name("Empresa ABC")
                .build();
        token = EmailToken.builder()
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .tokenHash("hash-token")
                .type(EmailTokenType.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(tokenService.hashToken("raw-token")).thenReturn("hash-token");
    }

    @Test
    void marksEmailVerifiedAndConsumesToken() {
        when(emailTokenRepository.findByTokenHashAndType("hash-token", EmailTokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(token));
        when(userRepository.findByIdAndTenantId(user.getId(), user.getTenantId()))
                .thenReturn(Optional.of(user));
        when(tenantRepository.findById(user.getTenantId()))
                .thenReturn(Optional.of(tenant));

        service.execute(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().isEmailVerified()).isTrue();

        ArgumentCaptor<EmailToken> tokenCaptor = ArgumentCaptor.forClass(EmailToken.class);
        verify(emailTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().isUsed()).isTrue();

        verify(authEmailSender).sendWelcome(any(User.class), any(Tenant.class));
    }

    @Test
    void failsWhenTokenIsInvalid() {
        when(emailTokenRepository.findByTokenHashAndType("hash-token", EmailTokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.auth.email_token_invalid");

        verify(userRepository, never()).save(any());
    }

    @Test
    void failsWhenTokenIsExpired() {
        token.setExpiresAt(Instant.now().minusSeconds(10));
        when(emailTokenRepository.findByTokenHashAndType("hash-token", EmailTokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.auth.email_token_invalid");
    }
}
