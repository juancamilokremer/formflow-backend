package com.kodelabs.formflow.modules.notifications.application.composer;

import com.kodelabs.formflow.modules.notifications.domain.model.EmailMessage;
import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;
import com.kodelabs.formflow.modules.notifications.domain.port.out.TemplateRendererPort;
import com.kodelabs.formflow.shared.i18n.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Expected model: candidateName, convocatoriaName, tenantName.
 */
@Component
@RequiredArgsConstructor
public class CandidateResponseConfirmationEmailComposer implements EmailComposer {

    private final TemplateRendererPort templateRenderer;
    private final Messages messages;

    @Override
    public EmailType type() {
        return EmailType.CANDIDATE_RESPONSE_CONFIRMATION;
    }

    @Override
    public EmailMessage compose(String to, Map<String, Object> model) {
        String subject = messages.get("email.candidate.confirmation.subject",
                model.get("convocatoriaName"));
        return new EmailMessage(to, subject,
                templateRenderer.render("candidate-response-confirmation", model));
    }
}
