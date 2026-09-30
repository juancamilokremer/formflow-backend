package com.kodelabs.formflow.modules.auth.application.usecase.invitation;

import com.kodelabs.formflow.modules.auth.domain.model.InvitationStatus;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetInvitationQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.InvitationPreviewResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserInvitationRepositoryPort;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetInvitationServiceTest {

    @Mock private UserInvitationRepositoryPort invitationRepository;
    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private TokenServicePort tokenService;
    @InjectMocks private GetInvitationService service;

    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lenient().when(tokenService.hashToken("raw-token")).thenReturn("hashed-token");
    }

    @Test
    void returnsThePreviewForAUsableInvitation() {
        UserInvitation invitation = UserInvitation.builder()
                .tenantId(tenantId).email("x@y.com").role(UserRole.EDITOR)
                .status(InvitationStatus.PENDING).expiresAt(Instant.now().plusSeconds(3600)).build();
        when(invitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(invitation));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(Tenant.builder().id(tenantId).name("Empresa ABC").build()));

        InvitationPreviewResult result = service.execute(new GetInvitationQuery("raw-token"));

        assertThat(result.tenantName()).isEqualTo("Empresa ABC");
        assertThat(result.email()).isEqualTo("x@y.com");
        assertThat(result.role()).isEqualTo(UserRole.EDITOR);
    }

    @Test
    void returns404WhenTheTokenDoesNotExist() {
        when(invitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetInvitationQuery("raw-token")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void returns410WhenTheInvitationExpired() {
        UserInvitation invitation = UserInvitation.builder()
                .tenantId(tenantId).email("x@y.com").role(UserRole.EDITOR)
                .status(InvitationStatus.PENDING).expiresAt(Instant.now().minusSeconds(3600)).build();
        when(invitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.execute(new GetInvitationQuery("raw-token")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.GONE));
    }

    @Test
    void returns409WhenTheInvitationWasAlreadyAccepted() {
        UserInvitation invitation = UserInvitation.builder()
                .tenantId(tenantId).email("x@y.com").role(UserRole.EDITOR)
                .status(InvitationStatus.ACCEPTED).expiresAt(Instant.now().plusSeconds(3600)).build();
        when(invitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.execute(new GetInvitationQuery("raw-token")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void returns404WhenTheInvitationWasCancelled() {
        UserInvitation invitation = UserInvitation.builder()
                .tenantId(tenantId).email("x@y.com").role(UserRole.EDITOR)
                .status(InvitationStatus.CANCELLED).expiresAt(Instant.now().plusSeconds(3600)).build();
        when(invitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.execute(new GetInvitationQuery("raw-token")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
