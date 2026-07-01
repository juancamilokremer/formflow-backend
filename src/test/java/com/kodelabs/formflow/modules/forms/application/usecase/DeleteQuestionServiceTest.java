package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.application.usecase.question.DeleteQuestionService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.DeleteQuestionCommand;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteQuestionServiceTest {

    @Mock private FormLoader formLoader;
    @Mock private FormQuestionRepositoryPort questionRepository;
    @Mock private FormRepositoryPort formRepository;
    @InjectMocks private DeleteQuestionService service;

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
        sectionId = UUID.randomUUID();
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        question = FormQuestion.builder().id(questionId).sectionId(sectionId).formId(formId)
                .tenantId(tenantId).title("Q").type(QuestionType.TEXT).position(0).build();
        form = Form.builder().id(formId).tenantId(tenantId).name("F").type(FormType.CANDIDATES).version(1).build();
    }

    @Test
    void softDeletesQuestionAndIncrementsFormVersion() {
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.of(question));
        when(questionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new DeleteQuestionCommand(questionId, sectionId, formId, tenantId, userId));

        ArgumentCaptor<FormQuestion> qCaptor = ArgumentCaptor.forClass(FormQuestion.class);
        verify(questionRepository).save(qCaptor.capture());
        assertThat(qCaptor.getValue().isDeleted()).isTrue();

        ArgumentCaptor<Form> fCaptor = ArgumentCaptor.forClass(Form.class);
        verify(formRepository).save(fCaptor.capture());
        assertThat(fCaptor.getValue().getVersion()).isEqualTo(2);
    }

    @Test
    void throwsNotFoundWhenQuestionDoesNotExist() {
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.empty());

        var command = new DeleteQuestionCommand(questionId, sectionId, formId, tenantId, userId);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.question.not_found")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(formLoader, never()).loadOrThrow(any(), any());
    }
}
