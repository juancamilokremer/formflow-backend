package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.Condition;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogic;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConditionalLogicValidator {

    private final FormQuestionRepositoryPort questionRepository;

    public void validate(ConditionalLogic logic, UUID formId, UUID tenantId) {
        validate(logic, formId, tenantId, null);
    }

    public void validate(ConditionalLogic logic, UUID formId, UUID tenantId, UUID excludeQuestionId) {
        if (logic == null || logic.conditions() == null || logic.conditions().isEmpty()) return;

        Map<UUID, FormQuestion> questionMap = questionRepository
                .findActiveByFormIdAndTenantId(formId, tenantId)
                .stream()
                .collect(Collectors.toMap(FormQuestion::getId, q -> q));

        for (Condition condition : logic.conditions()) {
            if (excludeQuestionId != null && excludeQuestionId.equals(condition.sourceQuestionId())) {
                throw new BusinessException("error.question.conditional_self_reference", HttpStatus.BAD_REQUEST);
            }
            FormQuestion source = questionMap.get(condition.sourceQuestionId());
            if (source == null) {
                throw new BusinessException("error.question.conditional_source_not_found",
                        HttpStatus.BAD_REQUEST, condition.sourceQuestionId().toString());
            }
            if (!ConditionOperator.isValidFor(condition.operator(), source.getType())) {
                throw new BusinessException("error.question.conditional_operator_invalid",
                        HttpStatus.BAD_REQUEST, condition.operator().name(), source.getType().code());
            }
        }
    }
}
