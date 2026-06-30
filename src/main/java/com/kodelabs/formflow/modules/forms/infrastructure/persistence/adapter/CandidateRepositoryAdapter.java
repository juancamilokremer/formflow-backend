package com.kodelabs.formflow.modules.forms.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.mapper.CandidatePersistenceMapper;
import com.kodelabs.formflow.modules.forms.infrastructure.persistence.repository.CandidateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CandidateRepositoryAdapter implements CandidateRepositoryPort {

    private final CandidateJpaRepository jpaRepository;
    private final CandidatePersistenceMapper mapper;

    @Override
    public Candidate save(Candidate candidate) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(candidate)));
    }

    @Override
    public List<Candidate> saveAll(List<Candidate> candidates) {
        return jpaRepository.saveAll(candidates.stream().map(mapper::toEntity).toList())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Candidate> findByIdAndConvocatoriaId(UUID id, UUID convocatoriaId) {
        return jpaRepository.findByIdAndConvocatoriaId(id, convocatoriaId).map(mapper::toDomain);
    }

    @Override
    public Optional<Candidate> findByToken(UUID token) {
        return jpaRepository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public List<Candidate> findAllByConvocatoriaId(UUID convocatoriaId) {
        return jpaRepository.findAllByConvocatoriaId(convocatoriaId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Candidate> findAllByIds(List<UUID> ids) {
        return jpaRepository.findAllByIdIn(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByConvocatoriaIdAndEmail(UUID convocatoriaId, String email) {
        return jpaRepository.existsByConvocatoriaIdAndEmail(convocatoriaId, email);
    }

    @Override
    public long countByConvocatoriaId(UUID convocatoriaId) {
        return jpaRepository.countByConvocatoriaId(convocatoriaId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
