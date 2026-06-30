package com.kodelabs.formflow.modules.forms.domain.port.out;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidateRepositoryPort {

    Candidate save(Candidate candidate);

    List<Candidate> saveAll(List<Candidate> candidates);

    Optional<Candidate> findByIdAndConvocatoriaId(UUID id, UUID convocatoriaId);

    Optional<Candidate> findByToken(UUID token);

    List<Candidate> findAllByConvocatoriaId(UUID convocatoriaId);

    List<Candidate> findAllByIds(List<UUID> ids);

    boolean existsByConvocatoriaIdAndEmail(UUID convocatoriaId, String email);

    long countByConvocatoriaId(UUID convocatoriaId);

    void deleteById(UUID id);
}
