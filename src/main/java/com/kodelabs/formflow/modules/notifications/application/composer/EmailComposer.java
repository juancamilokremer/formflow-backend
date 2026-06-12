package com.kodelabs.formflow.modules.notifications.application.composer;

import com.kodelabs.formflow.modules.notifications.domain.model.EmailMessage;
import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;

import java.util.Map;

/**
 * Strategy: one implementation per email type. Each composer knows its
 * subject key and template; Spring discovers all of them and SendEmailService
 * registers them by type. Adding an email never modifies existing code.
 */
public interface EmailComposer {

    EmailType type();

    EmailMessage compose(String to, Map<String, Object> model);
}
