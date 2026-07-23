package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaFormValidator;
import com.kodelabs.formflow.modules.forms.application.service.ConvocatoriaWeightValidator;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ScoringConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.CreateConvocatoriaUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateConvocatoriaCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
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
    private final ConvocatoriaFormValidator formValidator;
    private final ConvocatoriaWeightValidator weightValidator;

    @Override
    @Transactional
    public ConvocatoriaResult execute(CreateConvocatoriaCommand command) {
        formValidator.validateExists(command.formId(), command.tenantId());
        validateType(command.type());
        weightValidator.validate(command.categoryWeights());
        Convocatoria saved = convocatoriaRepository.save(buildConvocatoria(command));
        return ConvocatoriaResult.from(saved, List.of());
    }

    private void validateType(FormType type) {
        if (type == FormType.REGISTRATION) {
            throw new BusinessException("error.convocatoria.invalid_type", HttpStatus.BAD_REQUEST);
        }
    }

    private Convocatoria buildConvocatoria(CreateConvocatoriaCommand command) {
        return Convocatoria.builder()
                .tenantId(command.tenantId())
                .formId(command.formId())
                .name(command.name())
                .type(command.type())
                .categoryWeights(command.categoryWeights() != null ? command.categoryWeights() : List.of())
                .scoringConfig(command.scoringConfig() != null ? command.scoringConfig() : ScoringConfig.defaults())
                .build();
    }
}
