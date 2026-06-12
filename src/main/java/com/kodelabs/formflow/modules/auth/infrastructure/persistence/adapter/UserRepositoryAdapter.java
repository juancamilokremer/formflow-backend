package com.kodelabs.formflow.modules.auth.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.UserRepositoryPort;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia: implementa el puerto del dominio usando Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public User save(User user) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findByEmailAndTenantId(String email, UUID tenantId) {
        return jpaRepository.findByEmailAndTenantId(email, tenantId).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.findByIdAndTenantId(id, tenantId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmailAndTenantId(String email, UUID tenantId) {
        return jpaRepository.existsByEmailAndTenantId(email, tenantId);
    }
}
