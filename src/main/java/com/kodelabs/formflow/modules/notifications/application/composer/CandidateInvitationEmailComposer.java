package com.kodelabs.formflow.modules.notifications.application.composer;

import com.kodelabs.formflow.modules.notifications.domain.model.EmailMessage;
import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;
import com.kodelabs.formflow.modules.notifications.domain.port.out.TemplateRendererPort;
import com.kodelabs.formflow.shared.i18n.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Expected model: candidateName, convocatoriaName, tenantName, surveyUrl, isSurvey.
 */
@Component
@RequiredArgsConstructor
public class CandidateInvitationEmailComposer implements EmailComposer {

    private final TemplateRendererPort templateRenderer;
    private final Messages messages;

    @Override
    public EmailType type() {
        return EmailType.CANDIDATE_INVITATION;
    }

    @Override
    public EmailMessage compose(String to, Map<String, Object> model) {
        boolean isSurvey = Boolean.TRUE.equals(model.get("isSurvey"));
        String subjectKey = isSurvey ? "email.survey.invitation.subject" : "email.candidate.invitation.subject";
        String subject = messages.get(subjectKey, model.get("convocatoriaName"));
        return new EmailMessage(to, subject,
                templateRenderer.render("candidate-invitation", model));
    }
}
