package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.port.in.RemoveCandidateUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.RemoveCandidateCommand;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveCandidateService implements RemoveCandidateUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final CandidateRepositoryPort candidateRepository;

    @Override
    @Transactional
    public void execute(RemoveCandidateCommand command) {
        validateConvocatoriaIsEditable(command);
        Candidate candidate = loadCandidate(command);
        candidateRepository.deleteById(candidate.getId());
    }

    private void validateConvocatoriaIsEditable(RemoveCandidateCommand command) {
        var convocatoria = convocatoriaRepository
                .findByIdAndTenantId(command.convocatoriaId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found",
                        HttpStatus.NOT_FOUND, command.convocatoriaId()));
        if (convocatoria.isClosed()) {
            throw new BusinessException("error.convocatoria.already_closed", HttpStatus.CONFLICT);
        }
    }

    private Candidate loadCandidate(RemoveCandidateCommand command) {
        return candidateRepository
                .findByIdAndConvocatoriaId(command.candidateId(), command.convocatoriaId())
                .orElseThrow(() -> new BusinessException("error.candidate.not_found",
                        HttpStatus.NOT_FOUND, command.candidateId()));
    }
}
