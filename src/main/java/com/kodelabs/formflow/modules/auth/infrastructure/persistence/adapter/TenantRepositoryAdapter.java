package com.kodelabs.formflow.modules.auth.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.modules.auth.domain.model.TenantStatus;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.mapper.TenantPersistenceMapper;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository.TenantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
        return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(tenant)));
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

    @Override
    public List<Tenant> findAll(int page, int size, TenantStatus status, TenantPlan plan) {
        return jpaRepository.findAll(status, plan, PageRequest.of(page, size))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countAll(TenantStatus status, TenantPlan plan) {
        return jpaRepository.countAll(status, plan);
    }

    @Override
    public Map<TenantPlan, Long> countByPlan() {
        Map<TenantPlan, Long> result = new EnumMap<>(TenantPlan.class);
        for (Object[] row : jpaRepository.countGroupedByPlan()) {
            result.put((TenantPlan) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }
}
