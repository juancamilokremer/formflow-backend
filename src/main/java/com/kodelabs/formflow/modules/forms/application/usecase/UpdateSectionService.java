package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateSectionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateSectionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SectionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateSectionService implements UpdateSectionUseCase {

    private final FormSectionRepositoryPort sectionRepository;

    @Override
    @Transactional
    public SectionResult execute(UpdateSectionCommand command) {
        FormSection section = sectionRepository
                .findByIdAndFormIdAndTenantId(command.sectionId(), command.formId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.section.not_found", HttpStatus.NOT_FOUND,
                        command.sectionId().toString()));

        section.setTitle(command.title());
        section.setDescription(command.description());

        return SectionResult.from(sectionRepository.save(section));
    }
}
