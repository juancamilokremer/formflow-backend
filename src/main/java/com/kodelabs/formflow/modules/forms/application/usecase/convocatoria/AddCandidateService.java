package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.port.in.AddCandidateUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddCandidateCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AddCandidateService implements AddCandidateUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final CandidateRepositoryPort candidateRepository;

    @Override
    @Transactional
    public CandidateResult execute(AddCandidateCommand command) {
        Convocatoria convocatoria = loadOpenConvocatoria(command);
        validateNoDuplicate(convocatoria.getId(), command.email());
        Candidate saved = candidateRepository.save(buildCandidate(command, convocatoria.getId()));
        return CandidateResult.from(saved);
    }

    private Convocatoria loadOpenConvocatoria(AddCandidateCommand command) {
        Convocatoria convocatoria = convocatoriaRepository
                .findByIdAndTenantId(command.convocatoriaId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found",
                        HttpStatus.NOT_FOUND, command.convocatoriaId()));
        if (convocatoria.isClosed()) {
            throw new BusinessException("error.convocatoria.already_closed", HttpStatus.CONFLICT);
        }
        return convocatoria;
    }

    private void validateNoDuplicate(java.util.UUID convocatoriaId, String email) {
        if (candidateRepository.existsByConvocatoriaIdAndEmail(convocatoriaId, email)) {
            throw new BusinessException("error.candidate.email_already_exists", HttpStatus.CONFLICT, email);
        }
    }

    private Candidate buildCandidate(AddCandidateCommand command, java.util.UUID convocatoriaId) {
        return Candidate.builder()
                .convocatoriaId(convocatoriaId)
                .tenantId(command.tenantId())
                .name(command.name())
                .email(command.email())
                .invitedAt(Instant.now())
                .build();
    }
}
