package com.kodelabs.formflow.modules.notifications.domain.port.in;

import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;

import java.util.Map;

/**
 * Input port: single entry point other modules use to send emails.
 * The implementation dispatches to the right EmailComposer (Strategy)
 * and delivers asynchronously — callers never block on SMTP.
 */
public interface SendEmailUseCase {

    void send(EmailType type, String to, Map<String, Object> model);
}
