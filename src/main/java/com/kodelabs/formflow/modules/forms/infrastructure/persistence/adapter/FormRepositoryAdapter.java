package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.FormPersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.FormJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FormRepositoryAdapter implements FormRepositoryPort {

    private final FormJpaRepository formJpa;
    private final FormPersistenceMapper formMapper;
    private final FormSectionRepositoryPort sectionRepository;

    @Override
    public Form save(Form form) {
        return formMapper.toDomain(formJpa.save(formMapper.toEntity(form)));
    }

    @Override
    public Optional<Form> findByIdAndTenantId(UUID id, UUID tenantId) {
        return formJpa.findActiveByIdAndTenantId(id, tenantId).map(formMapper::toDomain);
    }

    @Override
    public Optional<Form> findByIdAndTenantIdWithSections(UUID id, UUID tenantId) {
        return formJpa.findActiveByIdAndTenantId(id, tenantId).map(entity -> {
            Form form = formMapper.toDomain(entity);
            form.setSections(sectionRepository.findActiveByFormIdAndTenantId(id, tenantId));
            return form;
        });
    }

    @Override
    public List<Form> findAllByTenantId(UUID tenantId) {
        return formJpa.findAllActiveByTenantId(tenantId).stream()
                .map(formMapper::toDomain).toList();
    }

    @Override
    public boolean existsByIdAndTenantId(UUID id, UUID tenantId) {
        return formJpa.existsActiveByIdAndTenantId(id, tenantId);
    }
}
