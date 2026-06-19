package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.FormQuestionPersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.FormQuestionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FormQuestionRepositoryAdapter implements FormQuestionRepositoryPort {

    private final FormQuestionJpaRepository questionJpa;
    private final FormQuestionPersistenceMapper questionMapper;

    @Override
    public FormQuestion save(FormQuestion question) {
        return questionMapper.toDomain(questionJpa.save(questionMapper.toEntity(question)));
    }

    @Override
    public void saveAll(List<FormQuestion> questions) {
        questionJpa.saveAll(questions.stream().map(questionMapper::toEntity).toList());
    }

    @Override
    public Optional<FormQuestion> findByIdAndSectionIdAndTenantId(UUID id, UUID sectionId, UUID tenantId) {
        return questionJpa.findActiveByIdAndSectionIdAndTenantId(id, sectionId, tenantId)
                .map(questionMapper::toDomain);
    }

    @Override
    public List<FormQuestion> findActiveBySectionIdAndTenantId(UUID sectionId, UUID tenantId) {
        return questionJpa.findActiveBySectionIdAndTenantId(sectionId, tenantId)
                .stream().map(questionMapper::toDomain).toList();
    }

    @Override
    public int countActiveBySectionId(UUID sectionId) {
        return questionJpa.countActiveBySectionId(sectionId);
    }

    @Override
    public Map<UUID, List<FormQuestion>> findAllActiveBySectionIds(List<UUID> sectionIds) {
        if (sectionIds.isEmpty()) return Map.of();
        return questionJpa.findActiveBySectionIdIn(sectionIds).stream()
                .map(questionMapper::toDomain)
                .collect(Collectors.groupingBy(FormQuestion::getSectionId));
    }

    @Override
    public boolean existsActiveByCategoryIdAndTenantId(UUID categoryId, UUID tenantId) {
        return questionJpa.existsActiveByCategoryIdAndTenantId(categoryId, tenantId);
    }
}
