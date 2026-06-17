package com.kodelabs.formflow.modules.auth.infrastructure.scheduler;

import com.kodelabs.formflow.modules.auth.domain.port.out.RefreshTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Nightly job that removes expired refresh tokens.
 *
 * Revoked-but-not-yet-expired tokens are intentionally kept: if a revoked
 * token is presented again before it expires, the service detects the reuse
 * and revokes all active sessions of that user. Once expired, the token
 * cannot be replayed and is safe to delete.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepositoryPort refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void deleteExpiredTokens() {
        log.info("Token cleanup started");
        refreshTokenRepository.deleteAllExpired();
        log.info("Token cleanup finished");
    }
}
