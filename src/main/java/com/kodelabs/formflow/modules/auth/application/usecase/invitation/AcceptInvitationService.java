package com.kodelabs.formflow.modules.auth.application.usecase.invitation;

import com.kodelabs.formflow.modules.auth.application.service.AuthEmailSender;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;
import com.kodelabs.formflow.modules.auth.domain.port.in.AcceptInvitationUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.AcceptInvitationCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.AcceptInvitationResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserInvitationRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcceptInvitationService implements AcceptInvitationUseCase {

    private final UserInvitationRepositoryPort invitationRepository;
    private final TenantRepositoryPort tenantRepository;
    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenServicePort tokenService;
    private final AuthEmailSender authEmailSender;

    @Override
    @Transactional
    public AcceptInvitationResult execute(AcceptInvitationCommand command) {
        UserInvitation invitation = InvitationLookup.findUsableOrThrow(
                invitationRepository, tokenService, command.token());
        Tenant tenant = tenantRepository.findById(invitation.getTenantId())
                .orElseThrow(() -> new BusinessException("error.tenant.not_found"));

        User user = userRepository.save(User.builder()
                .tenantId(invitation.getTenantId())
                .email(invitation.getEmail())
                .passwordHash(passwordHasher.hash(command.password()))
                .firstName(command.firstName())
                .lastName(command.lastName())
                .role(invitation.getRole())
                .emailVerified(true) // accepting the emailed invite link already proves ownership
                .build());

        invitation.markAccepted();
        invitationRepository.save(invitation);

        authEmailSender.sendWelcome(user, tenant);

        return new AcceptInvitationResult(user.getId(), user.getEmail());
    }
}
