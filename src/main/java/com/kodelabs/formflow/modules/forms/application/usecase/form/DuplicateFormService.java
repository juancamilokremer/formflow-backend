package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.kodelabs.formflow.modules.forms.application.service.FormCloner;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.port.in.DuplicateFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DuplicateFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;
import com.kodelabs.formflow.shared.i18n.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DuplicateFormService implements DuplicateFormUseCase {

    private final FormLoader formLoader;
    private final FormCloner formCloner;
    private final Messages messages;

    @Override
    @Transactional
    public FormSummaryResult execute(DuplicateFormCommand command) {
        Form origin = formLoader.loadWithSectionsOrThrow(command.formId(), command.tenantId());

        String duplicateName = messages.get("form.duplicate_name_suffix", origin.getName());
        Form duplicate = formCloner.clone(origin, command.userId(), null, null, 1, duplicateName);

        return FormSummaryResult.of(duplicate, origin.getSections().size(), 0, null);
    }
}
