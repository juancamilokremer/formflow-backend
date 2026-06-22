package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.port.in.CreateFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.CreateFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateFormService implements CreateFormUseCase {

    private final FormRepositoryPort formRepository;

    @Override
    @Transactional
    public FormSummaryResult execute(CreateFormCommand command) {
        Form form = Form.builder()
                .tenantId(command.tenantId())
                .name(command.name())
                .description(command.description())
                .type(command.type())
                .timeLimitSeconds(command.timeLimitSeconds())
                .createdBy(command.userId())
                .updatedBy(command.userId())
                .build();

        Form saved = formRepository.save(form);
        return FormSummaryResult.of(saved, 0, 0, null);
    }
}
