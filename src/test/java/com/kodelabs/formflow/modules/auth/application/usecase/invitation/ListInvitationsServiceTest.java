package com.kodelabs.formflow.modules.auth.application.usecase.invitation;

import com.kodelabs.formflow.modules.auth.domain.model.InvitationStatus;
import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ListInvitationsQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.InvitationResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserInvitationRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListInvitationsServiceTest {

    @Mock private UserInvitationRepositoryPort invitationRepository;
    @InjectMocks private ListInvitationsService service;

    @Test
    void returnsThePendingInvitations() {
        UUID tenantId = UUID.randomUUID();
        UserInvitation invitation = UserInvitation.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).email("x@y.com")
                .role(UserRole.EDITOR).status(InvitationStatus.PENDING).build();
        when(invitationRepository.findAllPendingByTenantId(tenantId)).thenReturn(List.of(invitation));

        List<InvitationResult> results = service.execute(new ListInvitationsQuery(tenantId));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).email()).isEqualTo("x@y.com");
    }
}
