package com.kodelabs.formflow.modules.auth.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.auth.domain.model.RefreshToken;
import com.kodelabs.formflow.modules.auth.domain.port.out.RefreshTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.mapper.RefreshTokenPersistenceMapper;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter: implements the domain port using Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository jpaRepository;
    private final RefreshTokenPersistenceMapper mapper;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(refreshToken)));
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(mapper::toDomain);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllByUserId(UUID userId) {
        // REQUIRES_NEW: the bulk revocation happens as a response to a security
        // event (token reuse) and must persist even if the triggering use case
        // ends up throwing an exception and rolling back its own transaction
        jpaRepository.revokeAllByUserId(userId, Instant.now());
    }

    @Override
    @Transactional
    public void deleteAllExpired() {
        jpaRepository.deleteAllExpiredBefore(Instant.now());
    }
}
