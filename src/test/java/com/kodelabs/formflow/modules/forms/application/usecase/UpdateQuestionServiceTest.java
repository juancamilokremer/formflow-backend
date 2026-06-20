package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.service.ConditionalLogicValidator;
import com.kodelabs.formflow.modules.forms.application.service.QuestionConfigFactory;
import com.kodelabs.formflow.modules.forms.application.usecase.question.UpdateQuestionService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.TextConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.UpdateQuestionCommand;
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

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateQuestionServiceTest {

    @Mock private FormQuestionRepositoryPort questionRepository;
    @Mock private FormRepositoryPort formRepository;
    @Mock private QuestionConfigFactory configFactory;
    @Mock private ConditionalLogicValidator conditionalLogicValidator;
    @InjectMocks private UpdateQuestionService service;

    private UUID questionId;
    private UUID sectionId;
    private UUID formId;
    private UUID tenantId;
    private UUID userId;
    private FormQuestion question;
    private Form form;

    @BeforeEach
    void setUp() {
        questionId = UUID.randomUUID();
        sectionId  = UUID.randomUUID();
        formId     = UUID.randomUUID();
        tenantId   = UUID.randomUUID();
        userId     = UUID.randomUUID();
        question = FormQuestion.builder().id(questionId).sectionId(sectionId)
                .formId(formId).tenantId(tenantId).title("Vieja").type(new QuestionType("TEXT")).position(0).build();
        form = Form.builder().id(formId).tenantId(tenantId).name("F").type(FormType.CANDIDATES).version(2).build();
    }

    @Test
    void updatesAllFieldsAndReturnsResult() {
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.of(question));
        when(configFactory.build(any(), any())).thenReturn(new TextConfig());
        when(questionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuestionResult result = service.execute(new UpdateQuestionCommand(
                questionId, sectionId, formId, tenantId, userId,
                "Nueva", "Desc", new QuestionType("TEXT"), true, null, null, null, Map.of()));

        assertThat(result.title()).isEqualTo("Nueva");
        assertThat(result.required()).isTrue();
    }

    @Test
    void incrementsFormVersionOnUpdate() {
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.of(question));
        when(configFactory.build(any(), any())).thenReturn(new TextConfig());
        when(questionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new UpdateQuestionCommand(
                questionId, sectionId, formId, tenantId, userId,
                "T", null, new QuestionType("TEXT"), false, null, null, null, Map.of()));

        ArgumentCaptor<Form> captor = ArgumentCaptor.forClass(Form.class);
        verify(formRepository).save(captor.capture());
        assertThat(captor.getValue().getVersion()).isEqualTo(3);
    }

    @Test
    void throwsNotFoundWhenQuestionDoesNotExist() {
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.empty());

        var command = new UpdateQuestionCommand(
                questionId, sectionId, formId, tenantId, userId,
                "T", null, new QuestionType("TEXT"), false, null, null, null, Map.of());
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
