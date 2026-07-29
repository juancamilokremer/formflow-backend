package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.ConvocatoriaJpaEntity;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.ConvocatoriaFormPersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.ConvocatoriaPersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.ConvocatoriaFormJpaRepository;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.ConvocatoriaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConvocatoriaRepositoryAdapter implements ConvocatoriaRepositoryPort {

    private final ConvocatoriaJpaRepository jpaRepository;
    private final ConvocatoriaPersistenceMapper mapper;
    private final ConvocatoriaFormJpaRepository formJpaRepository;
    private final ConvocatoriaFormPersistenceMapper formMapper;

    @Override
    public Convocatoria save(Convocatoria convocatoria) {
        Convocatoria saved = mapper.toDomain(jpaRepository.save(mapper.toEntity(convocatoria)));
        saved.setForms(convocatoria.getForms());
        return saved;
    }

    @Override
    public Optional<Convocatoria> findByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.findByIdAndTenantId(id, tenantId).map(this::hydrate);
    }

    @Override
    public List<Convocatoria> findActiveByTenantId(UUID tenantId) {
        List<ConvocatoriaJpaEntity> entities = jpaRepository.findByTenantIdAndDeletedAtIsNull(tenantId);
        List<UUID> ids = entities.stream().map(ConvocatoriaJpaEntity::getId).toList();
        Map<UUID, List<ConvocatoriaForm>> formsByConvocatoriaId = formJpaRepository
                .findAllByConvocatoriaIdInOrderByPositionAsc(ids).stream()
                .map(formMapper::toDomain)
                .collect(Collectors.groupingBy(ConvocatoriaForm::getConvocatoriaId));
        return entities.stream()
                .map(mapper::toDomain)
                .peek(c -> c.setForms(formsByConvocatoriaId.getOrDefault(c.getId(), List.of())))
                .toList();
    }

    @Override
    public boolean existsByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.existsByIdAndTenantId(id, tenantId);
    }

    @Override
    public void softDeleteById(UUID id) {
        jpaRepository.softDeleteById(id, Instant.now());
    }

    private Convocatoria hydrate(ConvocatoriaJpaEntity entity) {
        Convocatoria convocatoria = mapper.toDomain(entity);
        convocatoria.setForms(formJpaRepository.findAllByConvocatoriaIdOrderByPositionAsc(entity.getId())
                .stream().map(formMapper::toDomain).toList());
        return convocatoria;
    }
}
