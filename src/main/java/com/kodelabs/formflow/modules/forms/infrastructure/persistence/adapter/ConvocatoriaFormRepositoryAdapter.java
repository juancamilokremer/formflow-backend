package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaFormRepositoryPort;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.ConvocatoriaFormPersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.ConvocatoriaFormJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConvocatoriaFormRepositoryAdapter implements ConvocatoriaFormRepositoryPort {

    private final ConvocatoriaFormJpaRepository jpaRepository;
    private final ConvocatoriaFormPersistenceMapper mapper;

    @Override
    public ConvocatoriaForm save(ConvocatoriaForm form) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(form)));
    }

    @Override
    public Optional<ConvocatoriaForm> findByConvocatoriaId(UUID convocatoriaId) {
        return jpaRepository.findByConvocatoriaId(convocatoriaId).map(mapper::toDomain);
    }
}
