package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateFormUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateFormCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateFormService implements UpdateFormUseCase {

    private final FormRepositoryPort formRepository;
    private final FormSectionRepositoryPort sectionRepository;

    @Override
    @Transactional
    public FormSummaryResult execute(UpdateFormCommand command) {
        Form form = formRepository
                .findByIdAndTenantId(command.formId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND,
                        command.formId().toString()));

        form.setName(command.name());
        form.setDescription(command.description());
        form.setTimeLimitSeconds(command.timeLimitSeconds());
        form.setUpdatedBy(command.userId());

        Form saved = formRepository.save(form);
        int sectionCount = sectionRepository.countActiveByFormId(saved.getId());
        return FormSummaryResult.of(saved, sectionCount);
    }
}
