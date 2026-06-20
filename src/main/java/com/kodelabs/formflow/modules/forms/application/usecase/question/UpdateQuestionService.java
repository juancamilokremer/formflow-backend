package com.kodelabs.formflow.modules.forms.application.usecase.question;

import com.kodelabs.formflow.modules.forms.application.service.ConditionalLogicValidator;
import com.kodelabs.formflow.modules.forms.application.service.QuestionConfigFactory;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateQuestionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateQuestionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateQuestionService implements UpdateQuestionUseCase {

    private final FormQuestionRepositoryPort questionRepository;
    private final FormRepositoryPort formRepository;
    private final QuestionConfigFactory configFactory;
    private final ConditionalLogicValidator conditionalLogicValidator;

    @Override
    @Transactional
    public QuestionResult execute(UpdateQuestionCommand command) {
        FormQuestion question = loadQuestion(command);
        conditionalLogicValidator.validate(
                command.conditionalLogic(), command.formId(), command.tenantId(), command.questionId());
        applyUpdates(question, command);
        FormQuestion saved = questionRepository.save(question);
        bumpFormVersion(command);
        return QuestionResult.from(saved);
    }

    private FormQuestion loadQuestion(UpdateQuestionCommand command) {
        return questionRepository
                .findByIdAndSectionIdAndTenantId(command.questionId(), command.sectionId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.question.not_found",
                        HttpStatus.NOT_FOUND, command.questionId().toString()));
    }

    private void applyUpdates(FormQuestion question, UpdateQuestionCommand command) {
        question.setTitle(command.title());
        question.setDescription(command.description());
        question.setType(command.type());
        question.setRequired(command.required());
        question.setCategoryId(command.categoryId());
        question.setTimeLimitSeconds(command.timeLimitSeconds());
        question.setConditionalLogic(command.conditionalLogic());
        question.setConfig(configFactory.build(command.type(), command.rawConfig()));
    }

    private void bumpFormVersion(UpdateQuestionCommand command) {
        Form form = formRepository
                .findByIdAndTenantId(command.formId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.form.not_found",
                        HttpStatus.NOT_FOUND, command.formId().toString()));
        form.incrementVersion();
        form.setUpdatedBy(command.userId());
        formRepository.save(form);
    }
}
