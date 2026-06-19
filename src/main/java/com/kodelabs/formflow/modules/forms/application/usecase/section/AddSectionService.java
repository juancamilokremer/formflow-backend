package com.kodelabs.formflow.modules.forms.application.usecase.section;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.port.in.AddSectionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddSectionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SectionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddSectionService implements AddSectionUseCase {

    private final FormRepositoryPort formRepository;
    private final FormSectionRepositoryPort sectionRepository;

    @Override
    @Transactional
    public SectionResult execute(AddSectionCommand command) {
        Form form = formRepository
                .findByIdAndTenantId(command.formId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.form.not_found", HttpStatus.NOT_FOUND,
                        command.formId().toString()));

        int nextPosition = sectionRepository.countActiveByFormId(form.getId());

        FormSection section = FormSection.builder()
                .formId(form.getId())
                .tenantId(command.tenantId())
                .title(command.title())
                .description(command.description())
                .position(nextPosition)
                .timeLimitSeconds(command.timeLimitSeconds())
                .build();

        FormSection saved = sectionRepository.save(section);

        form.incrementVersion();
        form.setUpdatedBy(command.userId());
        formRepository.save(form);

        return SectionResult.from(saved);
    }
}
