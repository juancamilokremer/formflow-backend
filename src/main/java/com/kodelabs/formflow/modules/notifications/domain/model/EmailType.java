package com.kodelabs.formflow.modules.notifications.domain.model;

/**
 * Email types the platform can send.
 * Adding a new type = new enum value + one EmailComposer implementation
 * + one Thymeleaf template + its message keys. Nothing else changes.
 */
public enum EmailType {
    WELCOME,
    PASSWORD_RESET,
    EMAIL_VERIFICATION
}
