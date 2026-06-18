package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.service.QuestionConfigFactory;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.port.in.AddQuestionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddQuestionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddQuestionService implements AddQuestionUseCase {

    private final FormRepositoryPort formRepository;
    private final FormSectionRepositoryPort sectionRepository;
    private final FormQuestionRepositoryPort questionRepository;
    private final QuestionConfigFactory configFactory;

    @Override
    @Transactional
    public QuestionResult execute(AddQuestionCommand command) {
        FormSection section = sectionRepository
                .findByIdAndFormIdAndTenantId(command.sectionId(), command.formId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.section.not_found",
                        HttpStatus.NOT_FOUND, command.sectionId().toString()));

        int nextPosition = questionRepository.countActiveBySectionId(section.getId());

        FormQuestion question = FormQuestion.builder()
                .sectionId(section.getId())
                .formId(command.formId())
                .tenantId(command.tenantId())
                .title(command.title())
                .description(command.description())
                .type(command.type())
                .position(nextPosition)
                .required(command.required())
                .categoryId(command.categoryId())
                .timeLimitSeconds(command.timeLimitSeconds())
                .config(configFactory.build(command.type(), command.rawConfig()))
                .build();

        FormQuestion saved = questionRepository.save(question);

        Form form = formRepository.findByIdAndTenantId(command.formId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.form.not_found",
                        HttpStatus.NOT_FOUND, command.formId().toString()));
        form.incrementVersion();
        form.setUpdatedBy(command.userId());
        formRepository.save(form);

        return QuestionResult.from(saved);
    }
}
