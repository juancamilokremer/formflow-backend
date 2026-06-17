package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.port.in.DeleteSectionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteSectionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteSectionService implements DeleteSectionUseCase {

    private final FormRepositoryPort formRepository;
    private final FormSectionRepositoryPort sectionRepository;

    @Override
    @Transactional
    public void execute(DeleteSectionCommand command) {
        FormSection section = sectionRepository
                .findByIdAndFormIdAndTenantId(command.sectionId(), command.formId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.section.not_found", HttpStatus.NOT_FOUND,
                        command.sectionId().toString()));

        section.softDelete();
        sectionRepository.save(section);

        Form form = formRepository
                .findByIdAndTenantId(command.formId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND,
                        command.formId().toString()));

        form.incrementVersion();
        form.setUpdatedBy(command.userId());
        formRepository.save(form);
    }
}
