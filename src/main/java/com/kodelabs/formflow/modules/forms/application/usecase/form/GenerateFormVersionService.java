package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.kodelabs.formflow.modules.forms.application.service.FormCloner;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.port.in.GenerateFormVersionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GenerateFormVersionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenerateFormVersionService implements GenerateFormVersionUseCase {

    private final FormLoader formLoader;
    private final FormRepositoryPort formRepository;
    private final FormCloner formCloner;

    @Override
    @Transactional
    public FormSummaryResult execute(GenerateFormVersionCommand command) {
        Form origin = formLoader.loadWithSectionsOrThrow(command.formId(), command.tenantId());
        origin.assertLockedForVersioning();

        UUID effectiveRoot = origin.getRootFormId() != null ? origin.getRootFormId() : origin.getId();
        int nextVersion = formRepository.findMaxVersionInFamily(effectiveRoot, command.tenantId()) + 1;

        Form newForm = formCloner.clone(origin, command.userId(), origin.getId(), effectiveRoot, nextVersion);

        return FormSummaryResult.of(newForm, origin.getSections().size(), 0, null);
    }
}
