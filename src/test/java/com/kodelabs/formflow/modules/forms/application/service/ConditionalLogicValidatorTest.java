package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.application.service.handler.QuestionTypeHandlerSpec;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.Condition;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogic;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionalLogicAction;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.ConditionOperator;
import com.kodelabs.formflow.modules.forms.domain.model.conditional.LogicOperator;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConditionalLogicValidatorTest {

    @Mock private FormQuestionRepositoryPort questionRepository;
    @Mock private QuestionTypeRegistry typeRegistry;
    @Mock private QuestionTypeHandlerSpec singleHandler;
    @InjectMocks private ConditionalLogicValidator validator;

    private UUID formId;
    private UUID tenantId;
    private UUID sourceId;
    private FormQuestion sourceQuestion;

    @BeforeEach
    void setUp() {
        formId   = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        sourceId = UUID.randomUUID();
        sourceQuestion = FormQuestion.builder()
                .id(sourceId).formId(formId).tenantId(tenantId)
                .type(new QuestionType("SINGLE")).build();
    }

    @Test
    void passesWhenConditionalLogicIsNull() {
        assertThatCode(() -> validator.validate(null, formId, tenantId))
                .doesNotThrowAnyException();
    }

    @Test
    void passesWhenConditionsListIsEmpty() {
        var logic = new ConditionalLogic(ConditionalLogicAction.SHOW, LogicOperator.AND, List.of());
        assertThatCode(() -> validator.validate(logic, formId, tenantId))
                .doesNotThrowAnyException();
    }

    @Test
    void passesWithValidCondition() {
        when(questionRepository.findActiveByFormIdAndTenantId(formId, tenantId))
                .thenReturn(List.of(sourceQuestion));
        when(typeRegistry.get(new QuestionType("SINGLE"))).thenReturn(singleHandler);
        when(singleHandler.supportedOperators()).thenReturn(Set.of(ConditionOperator.EQUALS, ConditionOperator.NOT_EQUALS));
        var logic = new ConditionalLogic(ConditionalLogicAction.SHOW, LogicOperator.AND,
                List.of(new Condition(sourceId, ConditionOperator.EQUALS, "opt-uuid")));

        assertThatCode(() -> validator.validate(logic, formId, tenantId))
                .doesNotThrowAnyException();
    }

    @Test
    void throwsWhenSourceQuestionNotInForm() {
        when(questionRepository.findActiveByFormIdAndTenantId(formId, tenantId))
                .thenReturn(List.of());
        var logic = new ConditionalLogic(ConditionalLogicAction.SHOW, LogicOperator.AND,
                List.of(new Condition(sourceId, ConditionOperator.EQUALS, "x")));

        assertThatThrownBy(() -> validator.validate(logic, formId, tenantId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void throwsWhenOperatorNotSupportedBySourceType() {
        when(questionRepository.findActiveByFormIdAndTenantId(formId, tenantId))
                .thenReturn(List.of(sourceQuestion));
        when(typeRegistry.get(new QuestionType("SINGLE"))).thenReturn(singleHandler);
        when(singleHandler.supportedOperators()).thenReturn(Set.of(ConditionOperator.EQUALS, ConditionOperator.NOT_EQUALS));
        var logic = new ConditionalLogic(ConditionalLogicAction.SHOW, LogicOperator.AND,
                List.of(new Condition(sourceId, ConditionOperator.GREATER_THAN, 5)));

        assertThatThrownBy(() -> validator.validate(logic, formId, tenantId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void throwsOnSelfReference() {
        UUID questionId = UUID.randomUUID();
        var logic = new ConditionalLogic(ConditionalLogicAction.SHOW, LogicOperator.AND,
                List.of(new Condition(questionId, ConditionOperator.EQUALS, "x")));

        assertThatThrownBy(() -> validator.validate(logic, formId, tenantId, questionId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
