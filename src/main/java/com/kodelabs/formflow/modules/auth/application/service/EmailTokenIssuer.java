package com.kodelabs.formflow.modules.auth.application.service;

import com.kodelabs.formflow.modules.auth.domain.model.EmailToken;
import com.kodelabs.formflow.modules.auth.domain.model.EmailTokenType;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.out.EmailTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Issues single-use email tokens. Invalidates any previous active token of
 * the same type — only the most recently emailed link works.
 * Returns the raw value (goes into the email link); only the hash is stored.
 */
@Component
@RequiredArgsConstructor
public class EmailTokenIssuer {

    private final EmailTokenRepositoryPort emailTokenRepository;
    private final TokenServicePort tokenService;

    public String issue(User user, EmailTokenType type, Duration validity) {
        emailTokenRepository.markAllUsedByUserIdAndType(user.getId(), type);

        String rawToken = tokenService.generateOpaqueToken();
        emailTokenRepository.save(EmailToken.builder()
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .tokenHash(tokenService.hashToken(rawToken))
                .type(type)
                .expiresAt(Instant.now().plus(validity))
                .build());
        return rawToken;
    }
}
