package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.FormSectionPersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.FormSectionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FormSectionRepositoryAdapter implements FormSectionRepositoryPort {

    private final FormSectionJpaRepository sectionJpa;
    private final FormSectionPersistenceMapper sectionMapper;

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

    @Override
    public Map<UUID, Integer> countAllActiveByFormIds(List<UUID> formIds) {
        if (formIds.isEmpty()) return Map.of();
        return sectionJpa.countActiveGroupByFormId(formIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> ((Long) row[1]).intValue()));
    }

    @Override
    public List<UUID> findActiveSectionIdsByFormId(UUID formId) {
        return sectionJpa.findActiveIdsByFormId(formId);
    }
}
