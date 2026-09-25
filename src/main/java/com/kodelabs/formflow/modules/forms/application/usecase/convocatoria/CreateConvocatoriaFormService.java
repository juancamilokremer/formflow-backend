package com.kodelabs.formflow.modules.forms.application.usecase.convocatoria;

import com.kodelabs.formflow.modules.forms.application.service.FormCloner;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.Convocatoria;
import com.kodelabs.formflow.modules.forms.domain.model.convocatoria.ConvocatoriaForm;
import com.kodelabs.formflow.modules.forms.domain.port.in.CreateConvocatoriaFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateConvocatoriaFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaFormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.ConvocatoriaRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import com.kodelabs.formflow.shared.i18n.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Creates a form and attaches it to its convocatoria in one transaction.
 *
 * Forms used to be created standalone and attached later by the caller, which left an orphan
 * behind whenever the second step never happened. Here there is no window: either both rows
 * exist or neither does.
 */
@Service
@RequiredArgsConstructor
public class CreateConvocatoriaFormService implements CreateConvocatoriaFormUseCase {

    private final ConvocatoriaRepositoryPort convocatoriaRepository;
    private final ConvocatoriaFormRepositoryPort convocatoriaFormRepository;
    private final FormRepositoryPort formRepository;
    private final FormLoader formLoader;
    private final FormCloner formCloner;
    private final Messages messages;

    @Override
    @Transactional
    public ConvocatoriaFormResult execute(CreateConvocatoriaFormCommand command) {
        Convocatoria convocatoria = loadDraftConvocatoria(command.convocatoriaId(), command.tenantId());
        validateSingleFormForRegistration(convocatoria);

        Form form = command.isDuplicate() ? duplicateForm(command) : createBlankForm(command, convocatoria);

        ConvocatoriaForm convocatoriaForm = ConvocatoriaForm.builder()
                .convocatoriaId(convocatoria.getId())
                .formId(form.getId())
                .weight(command.weight())
                .categoryWeights(command.categoryWeights() != null ? command.categoryWeights() : List.of())
                .minScore(command.minScore())
                .position(convocatoriaFormRepository.countByConvocatoriaId(convocatoria.getId()))
                .build();

        return ConvocatoriaFormResult.from(convocatoriaFormRepository.save(convocatoriaForm));
    }

    private Form createBlankForm(CreateConvocatoriaFormCommand command, Convocatoria convocatoria) {
        FormType type = command.type() != null ? command.type() : convocatoria.getType();
        return formRepository.save(Form.builder()
                .tenantId(command.tenantId())
                .name(command.name())
                .type(type)
                .createdBy(command.userId())
                .updatedBy(command.userId())
                .build());
    }

    private Form duplicateForm(CreateConvocatoriaFormCommand command) {
        Form origin = formLoader.loadWithSectionsOrThrow(command.duplicateFromId(), command.tenantId());
        String name = messages.get("form.duplicate_name_suffix", origin.getName());
        return formCloner.clone(origin, command.userId(), null, null, 1, name);
    }

    private Convocatoria loadDraftConvocatoria(UUID id, UUID tenantId) {
        // Locks the convocatoria row so a concurrent create-form on the same convocatoria
        // serializes instead of racing on the count()-then-insert position below (backend#162).
        Convocatoria convocatoria = convocatoriaRepository.findByIdAndTenantIdForUpdate(id, tenantId)
                .orElseThrow(() -> new BusinessException("error.convocatoria.not_found", HttpStatus.NOT_FOUND, id));
        if (!convocatoria.isDraft()) {
            throw new BusinessException("error.convocatoria.not_draft", HttpStatus.CONFLICT);
        }
        return convocatoria;
    }

    private void validateSingleFormForRegistration(Convocatoria convocatoria) {
        if (convocatoria.getType() == FormType.REGISTRATION && !convocatoria.getForms().isEmpty()) {
            throw new BusinessException("error.convocatoria.registration_single_form_only", HttpStatus.BAD_REQUEST);
        }
    }
}
