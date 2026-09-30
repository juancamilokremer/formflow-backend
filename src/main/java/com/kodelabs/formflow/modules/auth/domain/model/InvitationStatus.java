package com.kodelabs.formflow.modules.auth.domain.model;

/**
 * Persisted lifecycle of a UserInvitation. "Expired" is deliberately not a stored value —
 * same reasoning as EmailToken: it's computed from expiresAt, no cron job needed to flip it.
 */
public enum InvitationStatus {
    PENDING, ACCEPTED, CANCELLED
}
