package com.kodelabs.formflow.modules.notifications.application.composer;

import com.kodelabs.formflow.modules.notifications.domain.model.EmailMessage;
import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;
import com.kodelabs.formflow.modules.notifications.domain.port.out.TemplateRendererPort;
import com.kodelabs.formflow.shared.i18n.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Expected model: userName, verificationUrl, expirationHours.
 */
@Component
@RequiredArgsConstructor
public class EmailVerificationEmailComposer implements EmailComposer {

    private final TemplateRendererPort templateRenderer;
    private final Messages messages;

    @Override
    public EmailType type() {
        return EmailType.EMAIL_VERIFICATION;
    }

    @Override
    public EmailMessage compose(String to, Map<String, Object> model) {
        return new EmailMessage(to,
                messages.get("email.verification.subject"),
                templateRenderer.render("email-verification", model));
    }
}
