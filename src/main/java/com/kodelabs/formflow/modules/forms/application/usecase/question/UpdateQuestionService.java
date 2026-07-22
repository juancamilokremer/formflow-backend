package com.kodelabs.formflow.modules.forms.application.usecase.question;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.application.service.ConditionalLogicValidator;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.application.service.QuestionConfigFactory;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;
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

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UpdateQuestionService implements UpdateQuestionUseCase {

    private final FormLoader formLoader;
    private final FormQuestionRepositoryPort questionRepository;
    private final FormRepositoryPort formRepository;
    private final QuestionConfigFactory configFactory;
    private final ConditionalLogicValidator conditionalLogicValidator;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public QuestionResult execute(UpdateQuestionCommand command) {
        Form form = formLoader.loadOrThrow(command.formId(), command.tenantId());
        FormQuestion question = loadQuestion(command);
        conditionalLogicValidator.validate(
                command.conditionalLogic(), command.formId(), command.tenantId(), command.questionId());

        QuestionConfig newConfig = configFactory.build(command.type(), command.rawConfig());
        if (isStructuralChange(command, question, newConfig)) {
            form.assertEditable();
        }

        applyUpdates(question, command, newConfig);
        FormQuestion saved = questionRepository.save(question);
        bumpFormVersion(form, command);
        return QuestionResult.from(saved);
    }

    private boolean isStructuralChange(UpdateQuestionCommand command, FormQuestion question, QuestionConfig newConfig) {
        return command.type() != question.getType()
                || !Objects.equals(command.categoryId(), question.getCategoryId())
                || !objectMapper.valueToTree(newConfig).equals(objectMapper.valueToTree(question.getConfig()));
    }

    private FormQuestion loadQuestion(UpdateQuestionCommand command) {
        return questionRepository
                .findByIdAndSectionIdAndTenantId(command.questionId(), command.sectionId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.question.not_found",
                        HttpStatus.NOT_FOUND, command.questionId().toString()));
    }

    private void applyUpdates(FormQuestion question, UpdateQuestionCommand command, QuestionConfig newConfig) {
        if (command.categoryId() != null && !command.type().supportsScoring()) {
            throw new BusinessException("error.question.category_not_scoreable",
                    HttpStatus.BAD_REQUEST, command.type().code());
        }

        question.setTitle(command.title());
        question.setDescription(command.description());
        question.setType(command.type());
        question.setRequired(command.required());
        question.setCategoryId(command.categoryId());
        question.setTimeLimitSeconds(command.timeLimitSeconds());
        question.setConditionalLogic(command.conditionalLogic());
        question.setConfig(newConfig);
    }

    private void bumpFormVersion(Form form, UpdateQuestionCommand command) {
        form.incrementVersion();
        form.setUpdatedBy(command.userId());
        formRepository.save(form);
    }
}
