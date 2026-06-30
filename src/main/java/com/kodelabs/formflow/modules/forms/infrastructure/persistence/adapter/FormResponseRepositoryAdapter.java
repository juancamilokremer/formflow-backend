package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.AnswerValueJpaEntity;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.FormResponseJpaEntity;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.AnswerValuePersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.FormResponsePersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.AnswerValueJpaRepository;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.FormResponseJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FormResponseRepositoryAdapter implements FormResponseRepositoryPort {

    private final FormResponseJpaRepository responseJpa;
    private final AnswerValueJpaRepository answerJpa;
    private final FormResponsePersistenceMapper responseMapper;
    private final AnswerValuePersistenceMapper answerMapper;

    @Override
    @Transactional
    public FormResponse save(FormResponse response) {
        FormResponseJpaEntity savedResponse = responseJpa.save(responseMapper.toEntity(response));

        if (response.getAnswers() != null && !response.getAnswers().isEmpty()) {
            answerJpa.deleteAllByResponseId(savedResponse.getId());
            List<AnswerValueJpaEntity> answerEntities = response.getAnswers().stream()
                    .map(a -> answerMapper.toEntity(a, savedResponse.getId()))
                    .toList();
            answerJpa.saveAll(answerEntities);
        }

        List<AnswerValueJpaEntity> savedAnswers = answerJpa.findAllByResponseId(savedResponse.getId());
        return responseMapper.toDomain(savedResponse, savedAnswers);
    }

    @Override
    public Optional<FormResponse> findByIdAndTenantId(UUID id, UUID tenantId) {
        return responseJpa.findByIdAndTenantId(id, tenantId)
                .map(entity -> responseMapper.toDomain(entity, answerJpa.findAllByResponseId(entity.getId())));
    }

    @Override
    public Optional<FormResponse> findByRespondentToken(UUID respondentToken) {
        return responseJpa.findByRespondentToken(respondentToken)
                .map(entity -> responseMapper.toDomain(entity, answerJpa.findAllByResponseId(entity.getId())));
    }

    @Override
    public boolean existsByRespondentToken(UUID respondentToken) {
        return responseJpa.existsByRespondentToken(respondentToken);
    }

    @Override
    public List<FormResponse> findAllByFormIdAndTenantId(UUID formId, UUID tenantId) {
        return responseJpa.findAllByFormIdAndTenantId(formId, tenantId).stream()
                .map(entity -> responseMapper.toDomain(entity, answerJpa.findAllByResponseId(entity.getId())))
                .toList();
    }

    @Override
    public List<FormResponse> findPageByFormIdAndTenantId(UUID formId, UUID tenantId, int page, int size) {
        return responseJpa.findPageByFormAndTenant(formId, tenantId, PageRequest.of(page, size))
                .stream()
                .map(entity -> responseMapper.toDomain(entity, List.of()))
                .toList();
    }

    @Override
    public long countByFormIdAndTenantId(UUID formId, UUID tenantId) {
        return responseJpa.countByFormIdAndTenantId(formId, tenantId);
    }

    @Override
    public Map<UUID, Integer> countByFormIds(List<UUID> formIds) {
        Map<UUID, Integer> result = new HashMap<>();
        for (Object[] row : responseJpa.countGroupedByFormIds(formIds)) {
            result.put((UUID) row[0], ((Number) row[1]).intValue());
        }
        return result;
    }

    @Override
    public Map<UUID, Instant> lastResponseAtByFormIds(List<UUID> formIds) {
        Map<UUID, Instant> result = new HashMap<>();
        for (Object[] row : responseJpa.lastCreatedAtGroupedByFormIds(formIds)) {
            result.put((UUID) row[0], (Instant) row[1]);
        }
        return result;
    }
}
