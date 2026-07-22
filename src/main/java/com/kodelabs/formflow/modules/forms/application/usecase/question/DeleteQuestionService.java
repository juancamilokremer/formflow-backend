package com.kodelabs.formflow.modules.forms.application.usecase.question;

import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
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

    private final FormLoader formLoader;
    private final FormQuestionRepositoryPort questionRepository;
    private final FormRepositoryPort formRepository;

    @Override
    @Transactional
    public void execute(DeleteQuestionCommand command) {
        Form form = formLoader.loadOrThrow(command.formId(), command.tenantId());
        if (form.isLocked()) {
            throw new BusinessException("error.question.form_locked", HttpStatus.BAD_REQUEST);
        }

        FormQuestion question = questionRepository
                .findByIdAndSectionIdAndTenantId(command.questionId(), command.sectionId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.question.not_found",
                        HttpStatus.NOT_FOUND, command.questionId().toString()));

        question.softDelete();
        questionRepository.save(question);

        form.incrementVersion();
        form.setUpdatedBy(command.userId());
        formRepository.save(form);
    }
}
