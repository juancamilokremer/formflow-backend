package com.kodelabs.formflow.modules.forms.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.application.service.ConditionalLogicValidator;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.application.service.QuestionConfigFactory;
import com.kodelabs.formflow.modules.forms.application.usecase.question.UpdateQuestionService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateQuestionServiceTest {

    @Mock private FormLoader formLoader;
    @Mock private FormQuestionRepositoryPort questionRepository;
    @Mock private FormRepositoryPort formRepository;
    @Mock private QuestionConfigFactory configFactory;
    @Mock private ConditionalLogicValidator conditionalLogicValidator;

    // Real instance (not mocked) — structural JsonNode comparison needs genuine behavior.
    private final ObjectMapper objectMapper = new ObjectMapper();

    private UpdateQuestionService service;

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
        service = new UpdateQuestionService(
                formLoader, questionRepository, formRepository, configFactory, conditionalLogicValidator, objectMapper);
        question = FormQuestion.builder().id(questionId).sectionId(sectionId)
                .formId(formId).tenantId(tenantId).title("Vieja").type(QuestionType.TEXT)
                .config(TextConfig.builder().maxLength(2000).build()).position(0).build();
        form = Form.builder().id(formId).tenantId(tenantId).name("F").type(FormType.CANDIDATES).version(2).build();
    }

    @Test
    void updatesAllFieldsAndReturnsResult() {
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.of(question));
        when(configFactory.build(any(), any())).thenReturn(new TextConfig());
        when(questionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuestionResult result = service.execute(new UpdateQuestionCommand(
                questionId, sectionId, formId, tenantId, userId,
                "Nueva", "Desc", QuestionType.TEXT, true, null, null, null, Map.of()));

        assertThat(result.title()).isEqualTo("Nueva");
        assertThat(result.required()).isTrue();
    }

    @Test
    void incrementsFormVersionOnUpdate() {
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.of(question));
        when(configFactory.build(any(), any())).thenReturn(new TextConfig());
        when(questionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new UpdateQuestionCommand(
                questionId, sectionId, formId, tenantId, userId,
                "T", null, QuestionType.TEXT, false, null, null, null, Map.of()));

        ArgumentCaptor<Form> captor = ArgumentCaptor.forClass(Form.class);
        verify(formRepository).save(captor.capture());
        assertThat(captor.getValue().getVersion()).isEqualTo(3);
    }

    @Test
    void throwsBadRequestWhenCategoryAssignedToNonScoreableType() {
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.of(question));

        var command = new UpdateQuestionCommand(
                questionId, sectionId, formId, tenantId, userId,
                "T", null, QuestionType.TEXT, false, UUID.randomUUID(), null, null, Map.of());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.question.category_not_scoreable")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        verify(questionRepository, never()).save(any());
    }

    @Test
    void allowsCategoryWhenTypeSupportsScoring() {
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.of(question));
        when(configFactory.build(any(), any())).thenReturn(new TextConfig());
        when(questionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateQuestionCommand(
                questionId, sectionId, formId, tenantId, userId,
                "T", null, QuestionType.SINGLE, false, UUID.randomUUID(), null, null, Map.of());

        assertThat(service.execute(command)).isNotNull();
        verify(questionRepository).save(any());
    }

    @Test
    void throwsNotFoundWhenQuestionDoesNotExist() {
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.empty());

        var command = new UpdateQuestionCommand(
                questionId, sectionId, formId, tenantId, userId,
                "T", null, QuestionType.TEXT, false, null, null, null, Map.of());
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void throwsBadRequestWhenFormIsLockedAndTypeChanges() {
        Form lockedForm = Form.builder().id(formId).tenantId(tenantId).name("F")
                .type(FormType.CANDIDATES).status(FormStatus.ACTIVE).version(2).build();
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(lockedForm);
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.of(question));
        when(configFactory.build(any(), any())).thenReturn(TextConfig.builder().maxLength(2000).build());

        var command = new UpdateQuestionCommand(
                questionId, sectionId, formId, tenantId, userId,
                "T", null, QuestionType.SINGLE, false, null, null, null, Map.of());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.question.form_locked")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        verify(questionRepository, never()).save(any());
    }

    @Test
    void throwsBadRequestWhenFormIsLockedAndCategoryChanges() {
        Form lockedForm = Form.builder().id(formId).tenantId(tenantId).name("F")
                .type(FormType.DIAGNOSTIC).status(FormStatus.ARCHIVED).version(2).build();
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(lockedForm);
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.of(question));
        when(configFactory.build(any(), any())).thenReturn(TextConfig.builder().maxLength(2000).build());

        var command = new UpdateQuestionCommand(
                questionId, sectionId, formId, tenantId, userId,
                "T", null, QuestionType.TEXT, false, UUID.randomUUID(), null, null, Map.of());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.question.form_locked")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        verify(questionRepository, never()).save(any());
    }

    @Test
    void throwsBadRequestWhenFormIsLockedAndConfigChanges() {
        Form lockedForm = Form.builder().id(formId).tenantId(tenantId).name("F")
                .type(FormType.CANDIDATES).status(FormStatus.ACTIVE).version(2).build();
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(lockedForm);
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.of(question));
        when(configFactory.build(any(), any())).thenReturn(TextConfig.builder().maxLength(500).build());

        var command = new UpdateQuestionCommand(
                questionId, sectionId, formId, tenantId, userId,
                "T", null, QuestionType.TEXT, false, null, null, null, Map.of());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.question.form_locked")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        verify(questionRepository, never()).save(any());
    }

    @Test
    void allowsCosmeticOnlyChangeWhenFormIsLocked() {
        Form lockedForm = Form.builder().id(formId).tenantId(tenantId).name("F")
                .type(FormType.CANDIDATES).status(FormStatus.ACTIVE).version(2).build();
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(lockedForm);
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.of(question));
        // Same type, same categoryId (null), same config content as the stored question — only title changes.
        when(configFactory.build(any(), any())).thenReturn(TextConfig.builder().maxLength(2000).build());
        when(questionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateQuestionCommand(
                questionId, sectionId, formId, tenantId, userId,
                "Nuevo título", null, QuestionType.TEXT, false, null, null, null, Map.of());

        QuestionResult result = service.execute(command);

        assertThat(result.title()).isEqualTo("Nuevo título");
        verify(questionRepository).save(any());
    }

    @Test
    void allowsStructuralChangeWhenFormIsRegistrationEvenIfActive() {
        Form registrationForm = Form.builder().id(formId).tenantId(tenantId).name("F")
                .type(FormType.REGISTRATION).status(FormStatus.ACTIVE).version(2).build();
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(registrationForm);
        when(questionRepository.findByIdAndSectionIdAndTenantId(questionId, sectionId, tenantId))
                .thenReturn(Optional.of(question));
        when(configFactory.build(any(), any())).thenReturn(TextConfig.builder().maxLength(500).build());
        when(questionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateQuestionCommand(
                questionId, sectionId, formId, tenantId, userId,
                "T", null, QuestionType.SINGLE, false, null, null, null, Map.of());

        assertThat(service.execute(command)).isNotNull();
        verify(questionRepository).save(any());
    }
}
