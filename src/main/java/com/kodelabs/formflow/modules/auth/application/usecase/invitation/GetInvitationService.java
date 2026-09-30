package com.kodelabs.formflow.modules.auth.application.usecase.invitation;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetInvitationUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetInvitationQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.InvitationPreviewResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserInvitationRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetInvitationService implements GetInvitationUseCase {

    private final UserInvitationRepositoryPort invitationRepository;
    private final TenantRepositoryPort tenantRepository;
    private final TokenServicePort tokenService;

    @Override
    public InvitationPreviewResult execute(GetInvitationQuery query) {
        UserInvitation invitation = InvitationLookup.findUsableOrThrow(
                invitationRepository, tokenService, query.token());
        Tenant tenant = tenantRepository.findById(invitation.getTenantId())
                .orElseThrow(() -> new BusinessException("error.tenant.not_found"));

        return new InvitationPreviewResult(tenant.getName(), invitation.getEmail(), invitation.getRole());
    }
}
