package com.kodelabs.formflow.modules.auth.application.usecase.invitation;

import com.kodelabs.formflow.modules.auth.application.service.AuthEmailSender;
import com.kodelabs.formflow.modules.auth.domain.model.InvitationStatus;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.AcceptInvitationCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.AcceptInvitationResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserInvitationRepositoryPort;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcceptInvitationServiceTest {

    @Mock private UserInvitationRepositoryPort invitationRepository;
    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private PasswordHasherPort passwordHasher;
    @Mock private TokenServicePort tokenService;
    @Mock private AuthEmailSender authEmailSender;
    @InjectMocks private AcceptInvitationService service;

    private final UUID tenantId = UUID.randomUUID();
    private UserInvitation invitation;

    @BeforeEach
    void setUp() {
        invitation = UserInvitation.builder()
                .tenantId(tenantId).email("nuevo@empresa.com").role(UserRole.EDITOR)
                .status(InvitationStatus.PENDING).expiresAt(Instant.now().plusSeconds(3600)).build();
        lenient().when(tokenService.hashToken("raw-token")).thenReturn("hashed-token");
        lenient().when(invitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(invitation));
        lenient().when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(Tenant.builder().id(tenantId).name("Empresa ABC").build()));
        lenient().when(passwordHasher.hash("password123")).thenReturn("$2a$hashed");
    }

    @Test
    void createsTheUserWithTheInvitedRoleAndMarksTheInvitationAccepted() {
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        var command = new AcceptInvitationCommand("raw-token", "Juan", "Perez", "password123");
        AcceptInvitationResult result = service.execute(command);

        assertThat(result.email()).isEqualTo("nuevo@empresa.com");
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.EDITOR);
        assertThat(captor.getValue().isEmailVerified()).isTrue();

        verify(authEmailSender).sendWelcome(any(), any());
    }

    @Test
    void rejectsAnExpiredInvitationWith410() {
        invitation.setExpiresAt(Instant.now().minusSeconds(3600));
        var command = new AcceptInvitationCommand("raw-token", "Juan", "Perez", "password123");

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.GONE));
    }

    @Test
    void rejectsAnAlreadyAcceptedInvitationWith409() {
        invitation.markAccepted();
        var command = new AcceptInvitationCommand("raw-token", "Juan", "Perez", "password123");

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void rejectsAnUnknownTokenWith404() {
        when(invitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.empty());
        var command = new AcceptInvitationCommand("raw-token", "Juan", "Perez", "password123");

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
