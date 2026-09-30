package com.kodelabs.formflow.modules.auth.application.usecase.invitation;

import com.kodelabs.formflow.modules.auth.domain.model.InvitationStatus;
import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.CancelInvitationCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserInvitationRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelInvitationServiceTest {

    @Mock private UserInvitationRepositoryPort invitationRepository;
    @InjectMocks private CancelInvitationService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID invitationId = UUID.randomUUID();

    @Test
    void marksAPendingInvitationAsCancelled() {
        UserInvitation invitation = UserInvitation.builder()
                .id(invitationId).tenantId(tenantId).email("x@y.com")
                .role(UserRole.EDITOR).status(InvitationStatus.PENDING).build();
        when(invitationRepository.findByIdAndTenantId(invitationId, tenantId)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(invitation)).thenReturn(invitation);

        service.execute(new CancelInvitationCommand(tenantId, invitationId));

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.CANCELLED);
    }

    @Test
    void rejectsCancellingAnAlreadyAcceptedInvitation() {
        UserInvitation invitation = UserInvitation.builder()
                .id(invitationId).tenantId(tenantId).email("x@y.com")
                .role(UserRole.EDITOR).status(InvitationStatus.ACCEPTED).build();
        when(invitationRepository.findByIdAndTenantId(invitationId, tenantId)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.execute(new CancelInvitationCommand(tenantId, invitationId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void throwsNotFoundWhenTheInvitationDoesNotExist() {
        when(invitationRepository.findByIdAndTenantId(invitationId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new CancelInvitationCommand(tenantId, invitationId)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
