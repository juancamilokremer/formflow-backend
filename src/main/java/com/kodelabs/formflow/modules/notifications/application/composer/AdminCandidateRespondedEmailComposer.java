package com.kodelabs.formflow.modules.notifications.application.composer;

import com.kodelabs.formflow.modules.notifications.domain.model.EmailMessage;
import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;
import com.kodelabs.formflow.modules.notifications.domain.port.out.TemplateRendererPort;
import com.kodelabs.formflow.shared.i18n.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Expected model: candidateName, candidateEmail, convocatoriaName, rankingUrl.
 */
@Component
@RequiredArgsConstructor
public class AdminCandidateRespondedEmailComposer implements EmailComposer {

    private final TemplateRendererPort templateRenderer;
    private final Messages messages;

    @Override
    public EmailType type() {
        return EmailType.ADMIN_CANDIDATE_RESPONDED;
    }

    @Override
    public EmailMessage compose(String to, Map<String, Object> model) {
        String subject = messages.get("email.admin.candidate_responded.subject",
                model.get("convocatoriaName"));
        return new EmailMessage(to, subject,
                templateRenderer.render("admin-candidate-responded", model));
    }
}
