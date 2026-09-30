package com.kodelabs.formflow.modules.auth.application.usecase.invitation;

import com.kodelabs.formflow.modules.auth.domain.model.InvitationStatus;
import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserInvitationRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Shared by GetInvitationService and AcceptInvitationService — both need the exact same
 * not-found/expired/already-accepted distinction (404/410/409, per #8's own acceptance
 * criteria), so it lives in one place instead of being duplicated twice.
 */
final class InvitationLookup {

    private InvitationLookup() {}

    static UserInvitation findUsableOrThrow(
            UserInvitationRepositoryPort invitationRepository, TokenServicePort tokenService, String rawToken) {
        UserInvitation invitation = invitationRepository.findByTokenHash(tokenService.hashToken(rawToken))
                .orElseThrow(() -> new BusinessException("error.user.invitation_not_found", HttpStatus.NOT_FOUND));

        if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
            throw new BusinessException("error.user.invitation_already_accepted", HttpStatus.CONFLICT);
        }
        if (invitation.getStatus() == InvitationStatus.CANCELLED) {
            throw new BusinessException("error.user.invitation_not_found", HttpStatus.NOT_FOUND);
        }
        if (invitation.isExpired()) {
            throw new BusinessException("error.user.invitation_expired", HttpStatus.GONE);
        }
        return invitation;
    }
}
