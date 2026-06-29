package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.service.ConditionalLogicEvaluator;
import com.kodelabs.formflow.modules.forms.application.service.FormSnapshotBuilder;
import com.kodelabs.formflow.modules.forms.application.usecase.response.SubmitPublicResponseService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AnswerItem;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.SubmitPublicResponseCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.SubmitPublicResponseResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitPublicResponseServiceTest {

    @Mock private FormRepositoryPort formRepository;
    @Mock private FormResponseRepositoryPort responseRepository;
    @Mock private FormSnapshotBuilder snapshotBuilder;
    @Mock private ConditionalLogicEvaluator conditionalLogicEvaluator;
    @InjectMocks private SubmitPublicResponseService service;

    private UUID formId;
    private UUID tenantId;
    private UUID questionId;
    private FormQuestion requiredQuestion;
    private FormSection section;
    private Form activeForm;
    private FormSnapshot snapshot;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        questionId = UUID.randomUUID();

        requiredQuestion = FormQuestion.builder()
                .id(questionId)
                .title("¿Años de experiencia?")
                .type(QuestionType.of("single"))
                .required(true)
                .position(1)
                .build();

        section = FormSection.builder()
                .id(UUID.randomUUID())
                .position(1)
                .questions(List.of(requiredQuestion))
                .build();

        activeForm = Form.builder()
                .id(formId)
                .tenantId(tenantId)
                .status(FormStatus.ACTIVE)
                .version(1)
                .sections(List.of(section))
                .build();

        snapshot = new FormSnapshot(formId, "Test Form", "CANDIDATES", 1, Instant.now(), List.of());
    }

    @Test
    void happyPath_savesResponseAndReturnsToken() {
        when(formRepository.findByIdPublicWithSections(formId)).thenReturn(Optional.of(activeForm));
        when(snapshotBuilder.build(formId, tenantId)).thenReturn(snapshot);
        when(conditionalLogicEvaluator.isVisible(any(), any(Map.class))).thenReturn(true);
        when(responseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubmitPublicResponseCommand command = new SubmitPublicResponseCommand(
                formId, null, List.of(new AnswerItem(questionId, "3 - 5 años")));

        SubmitPublicResponseResult result = service.execute(command);

        assertThat(result.respondentToken()).isNotNull();
        ArgumentCaptor<FormResponse> captor = ArgumentCaptor.forClass(FormResponse.class);
        org.mockito.Mockito.verify(responseRepository).save(captor.capture());
        assertThat(captor.getValue().getFormId()).isEqualTo(formId);
        assertThat(captor.getValue().getTenantId()).isEqualTo(tenantId);
        assertThat(captor.getValue().getAnswers()).hasSize(1);
    }

    @Test
    void visibleRequiredQuestionWithoutAnswer_throwsBadRequest() {
        when(formRepository.findByIdPublicWithSections(formId)).thenReturn(Optional.of(activeForm));
        when(conditionalLogicEvaluator.isVisible(any(), any(Map.class))).thenReturn(true);

        var command = new SubmitPublicResponseCommand(formId, null, List.of());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(be.getMessageKey()).isEqualTo("error.response.required_question_empty");
                });
    }

    @Test
    void hiddenRequiredQuestion_isNotValidated() {
        when(formRepository.findByIdPublicWithSections(formId)).thenReturn(Optional.of(activeForm));
        when(snapshotBuilder.build(formId, tenantId)).thenReturn(snapshot);
        // Question is hidden by conditional logic → no answer required
        when(conditionalLogicEvaluator.isVisible(any(), any(Map.class))).thenReturn(false);
        when(responseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new SubmitPublicResponseCommand(formId, null, List.of());
        SubmitPublicResponseResult result = service.execute(command);

        assertThat(result.respondentToken()).isNotNull();
    }
}
