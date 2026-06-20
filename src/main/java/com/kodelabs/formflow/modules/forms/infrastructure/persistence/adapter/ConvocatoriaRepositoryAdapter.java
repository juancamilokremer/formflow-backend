package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.ConvocatoriaPersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.ConvocatoriaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConvocatoriaRepositoryAdapter implements ConvocatoriaRepositoryPort {

    private final ConvocatoriaJpaRepository jpaRepository;
    private final ConvocatoriaPersistenceMapper mapper;

    @Override
    public Convocatoria save(Convocatoria convocatoria) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(convocatoria)));
    }

    @Override
    public Optional<Convocatoria> findByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.findByIdAndTenantId(id, tenantId).map(mapper::toDomain);
    }

    @Override
    public List<Convocatoria> findActiveByTenantId(UUID tenantId) {
        return jpaRepository.findByTenantIdAndDeletedAtIsNull(tenantId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.existsByIdAndTenantId(id, tenantId);
    }

    @Override
    public void softDeleteById(UUID id) {
        jpaRepository.softDeleteById(id, Instant.now());
    }
}
