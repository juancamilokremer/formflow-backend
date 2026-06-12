package com.kodelabs.formflow.modules.auth.domain.model;

import java.time.Instant;

/**
 * Value object for a freshly generated refresh token:
 * the raw value goes to the client, only the hash is persisted.
 */
public record GeneratedRefreshToken(String rawValue, String hash, Instant expiresAt) {}
