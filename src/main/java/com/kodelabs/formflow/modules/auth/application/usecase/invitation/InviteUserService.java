package com.kodelabs.formflow.modules.auth.application.usecase.invitation;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.InviteUserUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.InviteUserCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.InvitationResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserInvitationRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;
import com.kodelabs.formflow.modules.notifications.domain.port.in.SendEmailUseCase;
import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.i18n.Messages;
import com.kodelabs.formflow.shared.planlimit.PlanLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteUserService implements InviteUserUseCase {

    public static final Duration INVITATION_VALIDITY = Duration.ofHours(48);

    private final TenantRepositoryPort tenantRepository;
    private final UserRepositoryPort userRepository;
    private final UserInvitationRepositoryPort invitationRepository;
    private final PlanLimitService planLimitService;
    private final TokenServicePort tokenService;
    private final SendEmailUseCase sendEmail;
    private final Messages messages;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    @Transactional
    public InvitationResult execute(InviteUserCommand command) {
        planLimitService.checkUserLimit(command.tenantId());

        Tenant tenant = tenantRepository.findById(command.tenantId())
                .orElseThrow(() -> new BusinessException("error.tenant.not_found"));

        if (command.role() == UserRole.SUPER_ADMIN) {
            throw new BusinessException("error.user.invalid_role", HttpStatus.BAD_REQUEST);
        }

        String email = command.email().toLowerCase().trim();
        if (userRepository.existsByEmailAndTenantId(email, command.tenantId())) {
            throw new BusinessException("error.user.already_has_account", HttpStatus.CONFLICT);
        }

        cancelExistingPendingInvitation(email, command.tenantId());

        String rawToken = tokenService.generateOpaqueToken();
        UserInvitation invitation = invitationRepository.save(UserInvitation.builder()
                .tenantId(command.tenantId())
                .email(email)
                .role(command.role())
                .tokenHash(tokenService.hashToken(rawToken))
                .invitedByUserId(command.invitedByUserId())
                .expiresAt(Instant.now().plus(INVITATION_VALIDITY))
                .build());

        sendInvitationEmail(email, tenant, command.role(), rawToken);

        return InvitationResult.from(invitation);
    }

    /** Only the latest invitation to a given email is valid — same "reissue cancels the
     *  previous one" rule EmailTokenIssuer already applies to password-reset/verification. */
    private void cancelExistingPendingInvitation(String email, UUID tenantId) {
        if (!invitationRepository.existsPendingByEmailAndTenantId(email, tenantId)) return;
        invitationRepository.findAllPendingByTenantId(tenantId).stream()
                .filter(i -> i.getEmail().equals(email))
                .forEach(i -> {
                    i.markCancelled();
                    invitationRepository.save(i);
                });
    }

    private void sendInvitationEmail(String email, Tenant tenant, UserRole role, String rawToken) {
        Map<String, Object> model = new HashMap<>();
        model.put("tenantName", tenant.getName());
        model.put("roleLabel", messages.get("role." + role.name().toLowerCase()));
        model.put("acceptUrl", frontendBaseUrl + "/accept-invite?token=" + rawToken);
        model.put("expirationHours", INVITATION_VALIDITY.toHours());
        sendEmail.send(EmailType.USER_INVITATION, email, model);
    }
}
