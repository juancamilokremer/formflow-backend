package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.service.QuestionConfigFactory;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.port.in.UpdateQuestionUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateQuestionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateQuestionService implements UpdateQuestionUseCase {

    private final FormQuestionRepositoryPort questionRepository;
    private final QuestionConfigFactory configFactory;

    @Override
    @Transactional
    public QuestionResult execute(UpdateQuestionCommand command) {
        FormQuestion question = questionRepository
                .findByIdAndSectionIdAndTenantId(command.questionId(), command.sectionId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.question.not_found",
                        HttpStatus.NOT_FOUND, command.questionId().toString()));

        question.setTitle(command.title());
        question.setDescription(command.description());
        question.setType(command.type());
        question.setRequired(command.required());
        question.setCategoryId(command.categoryId());
        question.setTimeLimitSeconds(command.timeLimitSeconds());
        question.setConfig(configFactory.build(command.type(), command.rawConfig()));

        return QuestionResult.from(questionRepository.save(question));
    }
}
