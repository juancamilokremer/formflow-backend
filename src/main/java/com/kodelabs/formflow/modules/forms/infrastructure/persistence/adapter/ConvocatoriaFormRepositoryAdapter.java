package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaFormRepositoryPort;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.ConvocatoriaFormPersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.ConvocatoriaFormJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public List<ConvocatoriaForm> saveAll(List<ConvocatoriaForm> forms) {
        return jpaRepository.saveAll(forms.stream().map(mapper::toEntity).toList())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<ConvocatoriaForm> findAllByConvocatoriaId(UUID convocatoriaId) {
        return jpaRepository.findAllByConvocatoriaIdOrderByPositionAsc(convocatoriaId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public int countByConvocatoriaId(UUID convocatoriaId) {
        return jpaRepository.countByConvocatoriaId(convocatoriaId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
