package com.kodelabs.formflow.modules.auth.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.mapper.TenantPersistenceMapper;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository.TenantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter: implements the domain port using Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class TenantRepositoryAdapter implements TenantRepositoryPort {

    private final TenantJpaRepository jpaRepository;
    private final TenantPersistenceMapper mapper;

    @Override
    public Tenant save(Tenant tenant) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(tenant)));
    }

    @Override
    public Optional<Tenant> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Tenant> findBySlug(String slug) {
        return jpaRepository.findBySlug(slug).map(mapper::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }
}
