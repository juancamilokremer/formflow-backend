package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.usecase.form.GetFormScoringService;
import com.kodelabs.formflow.modules.forms.domain.model.Category;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.AnswerOption;
import com.kodelabs.formflow.modules.forms.domain.model.config.SingleConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormScoringQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormScoringResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.CategoryRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormQuestionRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormSectionRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFormScoringServiceTest {

    @Mock private FormRepositoryPort formRepository;
    @Mock private FormSectionRepositoryPort sectionRepository;
    @Mock private FormQuestionRepositoryPort questionRepository;
    @Mock private CategoryRepositoryPort categoryRepository;
    @InjectMocks private GetFormScoringService service;

    private UUID formId;
    private UUID tenantId;
    private UUID sectionId;
    private UUID categoryId;
    private Form form;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        sectionId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        form = Form.builder().id(formId).tenantId(tenantId).name("F").type(FormType.CANDIDATES).version(1).build();
    }

    @Test
    void returnsScoringResultGroupedByCategory() {
        FormQuestion q1 = FormQuestion.builder()
                .id(UUID.randomUUID()).sectionId(sectionId).formId(formId).tenantId(tenantId)
                .type(QuestionType.SINGLE).categoryId(categoryId)
                .config(SingleConfig.builder()
                        .options(List.of(
                                AnswerOption.builder().score(5).build(),
                                AnswerOption.builder().score(10).build()))
                        .build())
                .build();

        Category cat = Category.builder().id(categoryId).tenantId(tenantId)
                .name("Técnicas").color("#FF5733").build();

        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(sectionRepository.findActiveSectionIdsByFormId(formId)).thenReturn(List.of(sectionId));
        when(questionRepository.findAllActiveBySectionIds(List.of(sectionId)))
                .thenReturn(Map.of(sectionId, List.of(q1)));
        when(categoryRepository.findAllByIdsAndTenantId(List.of(categoryId), tenantId))
                .thenReturn(List.of(cat));

        FormScoringResult result = service.execute(new GetFormScoringQuery(formId, tenantId));

        assertThat(result.formId()).isEqualTo(formId);
        assertThat(result.categories()).hasSize(1);
        assertThat(result.categories().get(0).categoryId()).isEqualTo(categoryId);
        assertThat(result.categories().get(0).maxScore()).isEqualTo(10);
        assertThat(result.totalMaxScore()).isEqualTo(10);
    }

    @Test
    void returnsEmptyResultWhenFormHasNoQuestions() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.of(form));
        when(sectionRepository.findActiveSectionIdsByFormId(formId)).thenReturn(List.of());

        FormScoringResult result = service.execute(new GetFormScoringQuery(formId, tenantId));

        assertThat(result.categories()).isEmpty();
        assertThat(result.totalMaxScore()).isZero();
    }

    @Test
    void throwsNotFoundWhenFormDoesNotExist() {
        when(formRepository.findByIdAndTenantId(formId, tenantId)).thenReturn(Optional.empty());

        var query = new GetFormScoringQuery(formId, tenantId);
        assertThatThrownBy(() -> service.execute(query))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
