package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.service.ConditionalLogicValidator;
import com.kodelabs.formflow.modules.forms.application.service.FormLoader;
import com.kodelabs.formflow.modules.forms.application.service.QuestionConfigFactory;
import com.kodelabs.formflow.modules.forms.application.usecase.question.AddQuestionService;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.TextConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.AddQuestionCommand;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddQuestionServiceTest {

    @Mock private FormLoader formLoader;
    @Mock private FormRepositoryPort formRepository;
    @Mock private FormSectionRepositoryPort sectionRepository;
    @Mock private FormQuestionRepositoryPort questionRepository;
    @Mock private QuestionConfigFactory configFactory;
    @Mock private ConditionalLogicValidator conditionalLogicValidator;
    @InjectMocks private AddQuestionService service;

    private UUID formId;
    private UUID sectionId;
    private UUID tenantId;
    private UUID userId;
    private FormSection section;
    private Form form;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        sectionId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        section = FormSection.builder().id(sectionId).formId(formId).tenantId(tenantId).title("S").position(0).build();
        form = Form.builder().id(formId).tenantId(tenantId).name("F").type(FormType.CANDIDATES).version(1).build();
    }

    @Test
    void appendsQuestionAtNextPositionAndIncrementsFormVersion() {
        when(sectionRepository.findByIdAndFormIdAndTenantId(sectionId, formId, tenantId))
                .thenReturn(Optional.of(section));
        when(questionRepository.countActiveBySectionId(sectionId)).thenReturn(3);
        when(configFactory.build(any(), any())).thenReturn(new TextConfig());
        FormQuestion saved = FormQuestion.builder().id(UUID.randomUUID()).sectionId(sectionId)
                .formId(formId).title("Q").type(QuestionType.TEXT).position(3).build();
        when(questionRepository.save(any())).thenReturn(saved);
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuestionResult result = service.execute(new AddQuestionCommand(
                formId, sectionId, tenantId, userId, "Q", null, QuestionType.TEXT,
                false, null, null, null, Map.of()));

        assertThat(result.position()).isEqualTo(3);

        ArgumentCaptor<Form> formCaptor = ArgumentCaptor.forClass(Form.class);
        verify(formRepository).save(formCaptor.capture());
        assertThat(formCaptor.getValue().getVersion()).isEqualTo(2);
    }

    @Test
    void throwsBadRequestWhenCategoryAssignedToNonScoreableType() {
        when(sectionRepository.findByIdAndFormIdAndTenantId(sectionId, formId, tenantId))
                .thenReturn(Optional.of(section));

        var command = new AddQuestionCommand(
                formId, sectionId, tenantId, userId, "Q", null, QuestionType.TEXT,
                false, UUID.randomUUID(), null, null, Map.of());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.question.category_not_scoreable")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        verify(questionRepository, never()).save(any());
    }

    @Test
    void allowsCategoryWhenTypeSupportsScoring() {
        when(sectionRepository.findByIdAndFormIdAndTenantId(sectionId, formId, tenantId))
                .thenReturn(Optional.of(section));
        when(questionRepository.countActiveBySectionId(sectionId)).thenReturn(0);
        when(configFactory.build(any(), any())).thenReturn(new TextConfig());
        FormQuestion saved = FormQuestion.builder().id(UUID.randomUUID()).sectionId(sectionId)
                .formId(formId).title("Q").type(QuestionType.SINGLE).position(0).build();
        when(questionRepository.save(any())).thenReturn(saved);
        when(formLoader.loadOrThrow(formId, tenantId)).thenReturn(form);
        when(formRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new AddQuestionCommand(
                formId, sectionId, tenantId, userId, "Q", null, QuestionType.SINGLE,
                false, UUID.randomUUID(), null, null, Map.of());

        assertThat(service.execute(command)).isNotNull();
        verify(questionRepository).save(any());
    }

    @Test
    void throwsNotFoundWhenSectionDoesNotExist() {
        when(sectionRepository.findByIdAndFormIdAndTenantId(sectionId, formId, tenantId))
                .thenReturn(Optional.empty());

        var command = new AddQuestionCommand(
                formId, sectionId, tenantId, userId, "Q", null,
                QuestionType.TEXT, false, null, null, null, Map.of());
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.section.not_found")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
