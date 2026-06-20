package com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateScores;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CandidateStatus;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.entity.CandidateJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CandidatePersistenceMapper {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public Candidate toDomain(CandidateJpaEntity entity) {
        CandidateScores scores = entity.getScores() != null
                ? objectMapper.readValue(entity.getScores(), CandidateScores.class)
                : null;
        return Candidate.builder()
                .id(entity.getId())
                .convocatoriaId(entity.getConvocatoriaId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .email(entity.getEmail())
                .token(entity.getToken())
                .status(CandidateStatus.valueOf(entity.getStatus()))
                .responseId(entity.getResponseId())
                .scores(scores)
                .invitedAt(entity.getInvitedAt())
                .respondedAt(entity.getRespondedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @SneakyThrows
    public CandidateJpaEntity toEntity(Candidate domain) {
        String scores = domain.getScores() != null
                ? objectMapper.writeValueAsString(domain.getScores())
                : null;
        return CandidateJpaEntity.builder()
                .id(domain.getId())
                .convocatoriaId(domain.getConvocatoriaId())
                .tenantId(domain.getTenantId())
                .name(domain.getName())
                .email(domain.getEmail())
                .token(domain.getToken())
                .status(domain.getStatus().name())
                .responseId(domain.getResponseId())
                .scores(scores)
                .invitedAt(domain.getInvitedAt())
                .respondedAt(domain.getRespondedAt())
                .build();
    }
}
