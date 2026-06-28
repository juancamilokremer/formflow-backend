package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.usecase.question.ReorderQuestionsService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.ReorderQuestionsCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReorderQuestionsServiceTest {

    @Mock private FormQuestionRepositoryPort questionRepository;
    @Mock private FormRepositoryPort formRepository;
    @InjectMocks private ReorderQuestionsService service;

    private UUID formId;
    private UUID sectionId;
    private UUID tenantId;
    private UUID userId;
    private Form form;
    private UUID q1Id;
    private UUID q2Id;
    private UUID q3Id;
    private FormQuestion q1;
    private FormQuestion q2;
    private FormQuestion q3;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        sectionId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        form = Form.builder().id(formId).tenantId(tenantId).name("F").type(FormType.CANDIDATES).version(1).build();
        q1Id = UUID.randomUUID();
        q2Id = UUID.randomUUID();
        q3Id = UUID.randomUUID();
        q1 = FormQuestion.builder().id(q1Id).sectionId(sectionId).title("Q1").type(new QuestionType("TEXT")).position(0).build();
        q2 = FormQuestion.builder().id(q2Id).sectionId(sectionId).title("Q2").type(new QuestionType("SINGLE")).position(1).build();
        q3 = FormQuestion.builder().id(q3Id).sectionId(sectionId).title("Q3").type(new QuestionType("SCALE")).position(2).build();
    }

    @Test
    void assignsNewPositionsInRequestedOrderAndIncrementsVersion() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(questionRepository.findActiveBySectionIdAndTenantId(sectionId, tenantId))
                .thenReturn(List.of(q1, q2, q3));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<QuestionResult> results = service.execute(
                new ReorderQuestionsCommand(sectionId, formId, tenantId, userId,
                        List.of(q3Id, q1Id, q2Id)));

        assertThat(results.get(0).title()).isEqualTo("Q3");
        assertThat(results.get(0).position()).isZero();
        assertThat(results.get(1).title()).isEqualTo("Q1");
        assertThat(results.get(1).position()).isEqualTo(1);
        assertThat(results.get(2).title()).isEqualTo("Q2");
        assertThat(results.get(2).position()).isEqualTo(2);

        ArgumentCaptor<Form> formCaptor = ArgumentCaptor.forClass(Form.class);
        verify(formRepository).save(formCaptor.capture());
        assertThat(formCaptor.getValue().getVersion()).isEqualTo(2);
    }

    @Test
    void throwsBadRequestWhenOrderedIdsMissAQuestion() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(questionRepository.findActiveBySectionIdAndTenantId(sectionId, tenantId))
                .thenReturn(List.of(q1, q2, q3));

        var command = new ReorderQuestionsCommand(sectionId, formId, tenantId, userId, List.of(q1Id, q2Id));
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.question.reorder_invalid")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void throwsNotFoundWhenFormDoesNotExist() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.empty());

        var command = new ReorderQuestionsCommand(sectionId, formId, tenantId, userId, List.of(q1Id));
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.form.not_found")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
