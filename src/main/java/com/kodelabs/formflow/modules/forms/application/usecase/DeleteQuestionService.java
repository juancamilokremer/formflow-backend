package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.port.in.DeleteQuestionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteQuestionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteQuestionService implements DeleteQuestionUseCase {

    private final FormQuestionRepositoryPort questionRepository;
    private final FormRepositoryPort formRepository;

    @Override
    @Transactional
    public void execute(DeleteQuestionCommand command) {
        FormQuestion question = questionRepository
                .findByIdAndSectionIdAndTenantId(command.questionId(), command.sectionId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.question.not_found",
                        HttpStatus.NOT_FOUND, command.questionId().toString()));

        question.softDelete();
        questionRepository.save(question);

        Form form = formRepository.findByIdAndTenantId(command.formId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.form.not_found",
                        HttpStatus.NOT_FOUND, command.formId().toString()));
        form.incrementVersion();
        form.setUpdatedBy(command.userId());
        formRepository.save(form);
    }
}
