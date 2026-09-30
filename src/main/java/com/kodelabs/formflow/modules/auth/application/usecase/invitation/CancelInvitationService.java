package com.kodelabs.formflow.modules.auth.application.usecase.invitation;

import com.kodelabs.formflow.modules.auth.domain.model.InvitationStatus;
import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;
import com.kodelabs.formflow.modules.auth.domain.port.in.CancelInvitationUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.CancelInvitationCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserInvitationRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelInvitationService implements CancelInvitationUseCase {

    private final UserInvitationRepositoryPort invitationRepository;

    @Override
    @Transactional
    public void execute(CancelInvitationCommand command) {
        UserInvitation invitation = invitationRepository.findByIdAndTenantId(command.invitationId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.user.invitation_not_found", HttpStatus.NOT_FOUND));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessException("error.user.invitation_already_accepted", HttpStatus.CONFLICT);
        }

        invitation.markCancelled();
        invitationRepository.save(invitation);
    }
}
