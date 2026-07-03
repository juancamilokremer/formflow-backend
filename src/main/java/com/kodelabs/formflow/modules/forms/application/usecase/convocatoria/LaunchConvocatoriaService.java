package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaEmailSender;
import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaWeightValidator;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Candidate;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.port.in.LaunchConvocatoriaUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.LaunchConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LaunchConvocatoriaService implements LaunchConvocatoriaUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final ConvocatoriaWeightValidator weightValidator;
    private final ConvocatoriaEmailSender emailSender;

    @Override
    @Transactional
    public ConvocatoriaResult execute(LaunchConvocatoriaCommand command) {
        Convocatoria convocatoria = loadDraftConvocatoria(command);
        weightValidator.validate(convocatoria.getCategoryWeights());
        validateHasCandidates(convocatoria);
        convocatoria.launch();
        Convocatoria saved = convocatoriaRepository.save(convocatoria);
        List<Candidate> candidates = candidateRepository.findAllByConvocatoriaId(saved.getId());
        candidates.forEach(c -> emailSender.sendInvitation(c, saved));
        return ConvocatoriaResult.from(saved, candidates);
    }

    private Convocatoria loadDraftConvocatoria(LaunchConvocatoriaCommand command) {
        Convocatoria convocatoria = convocatoriaRepository
                .findByIdAndTenantId(command.id(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found",
                        HttpStatus.NOT_FOUND, command.id()));
        if (!convocatoria.isDraft()) {
            throw new BusinessException("error.convocatoria.not_draft", HttpStatus.CONFLICT);
        }
        return convocatoria;
    }

    private void validateHasCandidates(Convocatoria convocatoria) {
        if (candidateRepository.countByConvocatoriaId(convocatoria.getId()) == 0) {
            throw new BusinessException("error.convocatoria.no_candidates", HttpStatus.BAD_REQUEST);
        }
    }
}
