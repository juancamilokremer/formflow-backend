package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.FormResponsePersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.FormResponseJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FormResponseRepositoryAdapter implements FormResponseRepositoryPort {

    private final FormResponseJpaRepository responseJpa;
    private final FormResponsePersistenceMapper responseMapper;

    @Override
    public FormResponse save(FormResponse response) {
        return responseMapper.toDomain(responseJpa.save(responseMapper.toEntity(response)));
    }

    @Override
    public Optional<FormResponse> findByIdAndTenantId(UUID id, UUID tenantId) {
        return responseJpa.findByIdAndTenantId(id, tenantId).map(responseMapper::toDomain);
    }

    @Override
    public Optional<FormResponse> findByRespondentToken(UUID respondentToken) {
        return responseJpa.findByRespondentToken(respondentToken).map(responseMapper::toDomain);
    }

    @Override
    public boolean existsByRespondentToken(UUID respondentToken) {
        return responseJpa.existsByRespondentToken(respondentToken);
    }
}
