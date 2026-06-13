package com.kodelabs.formflow.modules.notifications.domain.model;

/**
 * A ready-to-send email. Built by an EmailComposer, delivered by EmailSenderPort.
 */
public record EmailMessage(String to, String subject, String htmlBody) {}
