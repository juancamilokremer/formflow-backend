package com.kodelabs.formflow.modules.notifications.application.usecase;

import com.kodelabs.formflow.modules.notifications.application.composer.EmailComposer;
import com.kodelabs.formflow.modules.notifications.domain.model.EmailType;
import com.kodelabs.formflow.modules.notifications.domain.port.in.SendEmailUseCase;
import com.kodelabs.formflow.modules.notifications.domain.port.out.EmailSenderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the SendEmailUseCase input port.
 *
 * Registry of composers (Strategy pattern): Spring injects every EmailComposer
 * bean and they get indexed by type — no switch/if grows with new emails.
 * Delivery is async so registration/login never block on SMTP; failures are
 * logged (with the caller's requestId thanks to the MDC-propagating executor).
 */
@Slf4j
@Service
public class SendEmailService implements SendEmailUseCase {

    private final Map<EmailType, EmailComposer> composers;
    private final EmailSenderPort emailSender;

    public SendEmailService(List<EmailComposer> composerList, EmailSenderPort emailSender) {
        this.composers = new EnumMap<>(EmailType.class);
        composerList.forEach(composer -> this.composers.put(composer.type(), composer));
        this.emailSender = emailSender;
    }

    @Async("emailExecutor")
    @Override
    public void send(EmailType type, String to, Map<String, Object> model) {
        EmailComposer composer = composers.get(type);
        if (composer == null) {
            log.error("No EmailComposer registered for type {}", type);
            return;
        }
        try {
            emailSender.deliver(composer.compose(to, model));
            log.info("Email {} sent to {}", type, to);
        } catch (Exception ex) {
            // Async: nobody upstream can catch this — log is the only evidence
            log.error("Failed to send {} email to {}: {}", type, to, ex.getMessage(), ex);
        }
    }
}
