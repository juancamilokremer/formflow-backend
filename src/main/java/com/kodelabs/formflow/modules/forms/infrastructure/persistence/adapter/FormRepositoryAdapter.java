package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.FormPersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.FormSectionPersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.FormJpaRepository;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.FormSectionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FormRepositoryAdapter implements FormRepositoryPort, FormSectionRepositoryPort {

    private final FormJpaRepository formJpa;
    private final FormSectionJpaRepository sectionJpa;
    private final FormPersistenceMapper formMapper;
    private final FormSectionPersistenceMapper sectionMapper;

    // ── FormRepositoryPort ──────────────────────────────────────────────────

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
            List<FormSection> sections = sectionJpa
                    .findActiveByFormIdAndTenantId(id, tenantId)
                    .stream().map(sectionMapper::toDomain).toList();
            form.setSections(sections);
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

    // ── FormSectionRepositoryPort ───────────────────────────────────────────

    @Override
    public FormSection save(FormSection section) {
        return sectionMapper.toDomain(sectionJpa.save(sectionMapper.toEntity(section)));
    }

    @Override
    public void saveAll(List<FormSection> sections) {
        sectionJpa.saveAll(sections.stream().map(sectionMapper::toEntity).toList());
    }

    @Override
    public Optional<FormSection> findByIdAndFormIdAndTenantId(UUID id, UUID formId, UUID tenantId) {
        return sectionJpa.findActiveByIdAndFormIdAndTenantId(id, formId, tenantId)
                .map(sectionMapper::toDomain);
    }

    @Override
    public List<FormSection> findActiveByFormIdAndTenantId(UUID formId, UUID tenantId) {
        return sectionJpa.findActiveByFormIdAndTenantId(formId, tenantId)
                .stream().map(sectionMapper::toDomain).toList();
    }

    @Override
    public int countActiveByFormId(UUID formId) {
        return sectionJpa.countActiveByFormId(formId);
    }
}
