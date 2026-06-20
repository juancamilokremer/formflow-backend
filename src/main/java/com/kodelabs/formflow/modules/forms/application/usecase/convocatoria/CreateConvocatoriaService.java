package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaWeightValidator;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.CreateConvocatoriaUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateConvocatoriaService implements CreateConvocatoriaUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final FormRepositoryPort formRepository;
    private final ConvocatoriaWeightValidator weightValidator;

    @Override
    @Transactional
    public ConvocatoriaResult execute(CreateConvocatoriaCommand command) {
        validateFormExists(command);
        weightValidator.validate(command.categoryWeights());
        Convocatoria saved = convocatoriaRepository.save(buildConvocatoria(command));
        return ConvocatoriaResult.from(saved, List.of());
    }

    private void validateFormExists(CreateConvocatoriaCommand command) {
        if (!formRepository.existsByIdAndTenantId(command.formId(), command.tenantId())) {
            throw new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND, command.formId());
        }
    }

    private Convocatoria buildConvocatoria(CreateConvocatoriaCommand command) {
        return Convocatoria.builder()
                .tenantId(command.tenantId())
                .formId(command.formId())
                .name(command.name())
                .categoryWeights(command.categoryWeights() != null ? command.categoryWeights() : List.of())
                .scoringConfig(command.scoringConfig() != null ? command.scoringConfig() : ScoringConfig.defaults())
                .build();
    }
}
