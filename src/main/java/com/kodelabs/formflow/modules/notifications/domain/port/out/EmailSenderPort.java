package com.kodelabs.formflow.modules.notifications.domain.port.out;

import com.kodelabs.formflow.modules.notifications.domain.model.EmailMessage;

/**
 * Output port for email delivery. Current adapter: SMTP (MailHog in dev,
 * SendGrid SMTP in prod). Swapping to an API-based provider only requires
 * a new adapter — composition logic is untouched.
 */
public interface EmailSenderPort {

    void deliver(EmailMessage message);
}
