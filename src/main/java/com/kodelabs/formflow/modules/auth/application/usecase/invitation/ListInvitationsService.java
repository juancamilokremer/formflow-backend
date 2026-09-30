package com.kodelabs.formflow.modules.auth.application.usecase.invitation;

import com.kodelabs.formflow.modules.auth.domain.port.in.ListInvitationsUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ListInvitationsQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.InvitationResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserInvitationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListInvitationsService implements ListInvitationsUseCase {

    private final UserInvitationRepositoryPort invitationRepository;

    @Override
    public List<InvitationResult> execute(ListInvitationsQuery query) {
        return invitationRepository.findAllPendingByTenantId(query.tenantId()).stream()
                .map(InvitationResult::from)
                .toList();
    }
}
