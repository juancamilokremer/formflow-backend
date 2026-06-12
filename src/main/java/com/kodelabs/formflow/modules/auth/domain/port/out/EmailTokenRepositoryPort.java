package com.kodelabs.formflow.modules.auth.domain.port.out;

import com.kodelabs.formflow.modules.auth.domain.model.EmailToken;
import com.kodelabs.formflow.modules.auth.domain.model.EmailTokenType;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port for EmailToken persistence operations.
 */
public interface EmailTokenRepositoryPort {

    EmailToken save(EmailToken emailToken);

    Optional<EmailToken> findByTokenHashAndType(String tokenHash, EmailTokenType type);

    /** Invalidates previous unused tokens when a new one is issued (only the latest is valid). */
    void markAllUsedByUserIdAndType(UUID userId, EmailTokenType type);
}
