package com.kodelabs.formflow.modules.forms.application.usecase.response;

import com.kodelabs.formflow.modules.forms.application.service.ConditionalLogicEvaluator;
import com.kodelabs.formflow.modules.forms.application.service.FormSnapshotBuilder;
import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.in.SubmitPublicResponseUseCase;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AnswerItem;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.SubmitPublicResponseCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SubmitPublicResponseResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmitPublicResponseService implements SubmitPublicResponseUseCase {

    private final FormRepositoryPort formRepository;
    private final FormResponseRepositoryPort responseRepository;
    private final FormSnapshotBuilder snapshotBuilder;
    private final ConditionalLogicEvaluator conditionalLogicEvaluator;

    @Override
    @Transactional
    public SubmitPublicResponseResult execute(SubmitPublicResponseCommand command) {
        Form form = formRepository.findByIdPublicWithSections(command.formId())
                .orElseThrow(() -> new BusinessException(
                        "error.form.not_found", HttpStatus.NOT_FOUND, command.formId().toString()));

        if (form.getStatus() != FormStatus.ACTIVE) {
            throw new BusinessException(
                    "error.form.not_found", HttpStatus.NOT_FOUND, command.formId().toString());
        }

        Map<UUID, Object> answerMap = buildAnswerMap(command.answers());
        validateRequiredQuestions(form, answerMap);

        FormSnapshot snapshot = snapshotBuilder.build(command.formId(), form.getTenantId());

        List<AnswerValue> answers = command.answers().stream()
                .map(item -> AnswerValue.builder()
                        .questionId(item.questionId())
                        .value(item.value())
                        .build())
                .toList();

        UUID respondentToken = UUID.randomUUID();
        FormResponse response = FormResponse.builder()
                .formId(form.getId())
                .tenantId(form.getTenantId())
                .respondentToken(respondentToken)
                .formSnapshot(snapshot)
                .answers(answers)
                .startedAt(command.startedAt())
                .build();

        responseRepository.save(response);
        return new SubmitPublicResponseResult(respondentToken);
    }

    private Map<UUID, Object> buildAnswerMap(List<AnswerItem> items) {
        Map<UUID, Object> map = new HashMap<>();
        if (items != null) {
            items.forEach(item -> map.put(item.questionId(), item.value()));
        }
        return map;
    }

    private void validateRequiredQuestions(Form form, Map<UUID, Object> answerMap) {
        for (FormSection section : form.getSections()) {
            for (FormQuestion question : section.getQuestions()) {
                boolean visible = conditionalLogicEvaluator.isVisible(
                        question.getConditionalLogic(), answerMap);
                if (visible && question.isRequired() && !isAnswered(answerMap, question.getId())) {
                    throw new BusinessException(
                            "error.response.required_question_empty",
                            HttpStatus.BAD_REQUEST,
                            question.getTitle());
                }
            }
        }
    }

    private boolean isAnswered(Map<UUID, Object> answerMap, UUID questionId) {
        if (!answerMap.containsKey(questionId)) return false;
        Object value = answerMap.get(questionId);
        if (value == null) return false;
        if (value instanceof String s) return !s.isBlank();
        if (value instanceof List<?> l) return !l.isEmpty();
        return true;
    }
}
