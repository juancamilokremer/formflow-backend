package com.kodelabs.formflow.modules.auth.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.auth.domain.model.EmailToken;
import com.kodelabs.formflow.modules.auth.domain.model.EmailTokenType;
import com.kodelabs.formflow.modules.auth.domain.port.out.EmailTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.mapper.EmailTokenPersistenceMapper;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository.EmailTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter: implements the domain port using Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class EmailTokenRepositoryAdapter implements EmailTokenRepositoryPort {

    private final EmailTokenJpaRepository jpaRepository;
    private final EmailTokenPersistenceMapper mapper;

    @Override
    public EmailToken save(EmailToken emailToken) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(emailToken)));
    }

    @Override
    public Optional<EmailToken> findByTokenHashAndType(String tokenHash, EmailTokenType type) {
        return jpaRepository.findByTokenHashAndType(tokenHash, type).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void markAllUsedByUserIdAndType(UUID userId, EmailTokenType type) {
        jpaRepository.markAllUsedByUserIdAndType(userId, type, Instant.now());
    }
}
