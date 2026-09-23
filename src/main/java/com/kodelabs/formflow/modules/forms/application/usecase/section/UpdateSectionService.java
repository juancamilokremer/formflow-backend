package com.kodelabs.formflow.modules.forms.application.usecase.section;

import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
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

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UpdateSectionService implements UpdateSectionUseCase {

    private final FormLoader formLoader;
    private final FormSectionRepositoryPort sectionRepository;

    @Override
    @Transactional
    public SectionResult execute(UpdateSectionCommand command) {
        FormSection section = sectionRepository
                .findByIdAndFormIdAndTenantId(command.sectionId(), command.formId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.section.not_found", HttpStatus.NOT_FOUND,
                        command.sectionId().toString()));

        // Title/description are plain copy — editable even on a locked form. The time limit
        // changes how respondents answer, so it counts as structural (same rule
        // UpdateQuestionService applies to type/category/config changes).
        if (!Objects.equals(command.timeLimitSeconds(), section.getTimeLimitSeconds())) {
            formLoader.loadOrThrow(command.formId(), command.tenantId()).assertEditable();
        }

        section.setTitle(command.title());
        section.setDescription(command.description());
        section.setTimeLimitSeconds(command.timeLimitSeconds());

        return SectionResult.from(sectionRepository.save(section));
    }
}
