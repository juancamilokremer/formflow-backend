package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.FormPersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.FormJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FormRepositoryAdapter implements FormRepositoryPort {

    private final FormJpaRepository formJpa;
    private final FormPersistenceMapper formMapper;
    private final FormSectionRepositoryPort sectionRepository;
    private final FormQuestionRepositoryPort questionRepository;

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
            List<FormSection> sections = sectionRepository.findActiveByFormIdAndTenantId(id, tenantId);

            List<UUID> sectionIds = sections.stream().map(FormSection::getId).toList();
            Map<UUID, List<FormQuestion>> questionsBySection =
                    questionRepository.findAllActiveBySectionIds(sectionIds);

            sections.forEach(s -> s.setQuestions(
                    questionsBySection.getOrDefault(s.getId(), List.of())));
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

    @Override
    public java.util.Optional<Form> findByIdPublicWithSections(UUID formId) {
        return formJpa.findActiveById(formId).map(entity -> {
            Form form = formMapper.toDomain(entity);
            List<FormSection> sections = sectionRepository.findActiveByFormIdAndTenantId(formId, form.getTenantId());
            List<UUID> sectionIds = sections.stream().map(FormSection::getId).toList();
            Map<UUID, List<FormQuestion>> questionsBySection =
                    questionRepository.findAllActiveBySectionIds(sectionIds);
            sections.forEach(s -> s.setQuestions(questionsBySection.getOrDefault(s.getId(), List.of())));
            form.setSections(sections);
            return form;
        });
    }

    @Override
    public int findMaxVersionInFamily(UUID rootId, UUID tenantId) {
        Integer max = formJpa.findMaxVersionInFamily(rootId, tenantId);
        return max != null ? max : 0;
    }
}
