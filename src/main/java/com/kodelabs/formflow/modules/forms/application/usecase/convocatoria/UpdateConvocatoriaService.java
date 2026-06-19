package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.CategoryWeight;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateConvocatoriaUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CandidateRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateConvocatoriaService implements UpdateConvocatoriaUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final CandidateRepositoryPort candidateRepository;

    @Override
    @Transactional
    public ConvocatoriaResult execute(UpdateConvocatoriaCommand command) {
        Convocatoria convocatoria = loadDraftConvocatoria(command.id(), command.tenantId());
        validateWeightsSum(command.categoryWeights());
        applyUpdates(convocatoria, command);
        Convocatoria saved = convocatoriaRepository.save(convocatoria);
        var candidates = candidateRepository.findAllByConvocatoriaId(saved.getId());
        return ConvocatoriaResult.from(saved, candidates);
    }

    private Convocatoria loadDraftConvocatoria(UUID id, UUID tenantId) {
        Convocatoria convocatoria = convocatoriaRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found", HttpStatus.NOT_FOUND, id));
        if (!convocatoria.isDraft()) {
            throw new BusinessException("error.convocatoria.not_draft", HttpStatus.CONFLICT);
        }
        return convocatoria;
    }

    private void validateWeightsSum(List<CategoryWeight> weights) {
        if (weights == null || weights.isEmpty()) return;
        int total = weights.stream().mapToInt(CategoryWeight::weight).sum();
        if (total != 100) {
            throw new BusinessException("error.convocatoria.weights_must_sum_100", HttpStatus.BAD_REQUEST, total);
        }
    }

    private void applyUpdates(Convocatoria convocatoria, UpdateConvocatoriaCommand command) {
        convocatoria.setName(command.name());
        if (command.categoryWeights() != null) convocatoria.setCategoryWeights(command.categoryWeights());
        if (command.scoringConfig() != null) convocatoria.setScoringConfig(command.scoringConfig());
    }
}
