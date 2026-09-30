package com.kodelabs.formflow.modules.notifications.application.composer;

import com.kodelabs.formflow.modules.notifications.domain.model.EmailMessage;
import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;
import com.kodelabs.formflow.modules.notifications.domain.port.out.TemplateRendererPort;
import com.kodelabs.formflow.shared.i18n.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Expected model: tenantName, role, acceptUrl, expirationHours.
 */
@Component
@RequiredArgsConstructor
public class UserInvitationEmailComposer implements EmailComposer {

    private final TemplateRendererPort templateRenderer;
    private final Messages messages;

    @Override
    public EmailType type() {
        return EmailType.USER_INVITATION;
    }

    @Override
    public EmailMessage compose(String to, Map<String, Object> model) {
        return new EmailMessage(to,
                messages.get("email.user_invitation.subject", model.get("tenantName")),
                templateRenderer.render("user-invitation", model));
    }
}
