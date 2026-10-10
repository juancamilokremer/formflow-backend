package com.kodelabs.formflow.modules.notifications.application.composer;

import com.kodelabs.formflow.modules.notifications.domain.model.EmailMessage;
import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;
import com.kodelabs.formflow.modules.notifications.domain.port.out.TemplateRendererPort;
import com.kodelabs.formflow.shared.i18n.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Expected model: tenantName, requesterName, requesterEmail, currentPlan,
 * requestedPlan, message (nullable).
 */
@Component
@RequiredArgsConstructor
public class PlanUpgradeRequestEmailComposer implements EmailComposer {

    private final TemplateRendererPort templateRenderer;
    private final Messages messages;

    @Override
    public EmailType type() {
        return EmailType.PLAN_UPGRADE_REQUEST;
    }

    @Override
    public EmailMessage compose(String to, Map<String, Object> model) {
        String subject = messages.get("email.plan_upgrade_request.subject", model.get("tenantName"));
        return new EmailMessage(to, subject, templateRenderer.render("plan-upgrade-request", model));
    }
}
