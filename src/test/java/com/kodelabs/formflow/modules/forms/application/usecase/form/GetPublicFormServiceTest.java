package com.kodelabs.formflow.modules.forms.application.usecase.form;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.application.service.QuestionConfigFactory;
import com.kodelabs.formflow.modules.forms.application.service.QuestionTypeRegistry;
import com.kodelabs.formflow.modules.forms.application.service.handler.SingleTypeHandler;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormStatus;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.TenantInfo;
import com.kodelabs.formflow.modules.forms.domain.model.config.AnswerOption;
import com.kodelabs.formflow.modules.forms.domain.model.config.SingleConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetPublicFormQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicFormResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.TenantInfoPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPublicFormServiceTest {

    @Mock private FormRepositoryPort formRepository;
    @Mock private TenantInfoPort tenantInfoPort;

    // Real instances (not mocked) — the redaction test needs the actual Strategy-pattern
    // dispatch through QuestionTypeRegistry to prove scores are genuinely stripped, not just
    // that some mock was called.
    private final QuestionConfigFactory configFactory =
            new QuestionConfigFactory(new QuestionTypeRegistry(List.of(new SingleTypeHandler(new ObjectMapper()))));

    private GetPublicFormService service;

    private UUID formId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        formId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        service = new GetPublicFormService(formRepository, tenantInfoPort, configFactory);
    }

    @Test
    void happyPath_includesTenantBrandingFromTenantInfoPort() {
        Form form = Form.builder()
                .id(formId).tenantId(tenantId)
                .name("Evaluacion de aspirantes").type(FormType.CANDIDATES)
                .status(FormStatus.ACTIVE)
                .build();
        TenantInfo tenantInfo = new TenantInfo("Kode Labs", "admin@kodelabs.com", "https://logo.png", "#123456");

        when(formRepository.findByIdPublicWithSections(formId)).thenReturn(Optional.of(form));
        when(tenantInfoPort.findByTenantId(tenantId)).thenReturn(Optional.of(tenantInfo));

        PublicFormResult result = service.execute(new GetPublicFormQuery(formId));

        assertThat(result.tenantName()).isEqualTo("Kode Labs");
        assertThat(result.tenantLogoUrl()).isEqualTo("https://logo.png");
        assertThat(result.tenantPrimaryColor()).isEqualTo("#123456");
    }

    @Test
    void tenantInfoMissing_returnsNullBrandingFields() {
        Form form = Form.builder()
                .id(formId).tenantId(tenantId)
                .name("Evaluacion").type(FormType.CANDIDATES)
                .status(FormStatus.ACTIVE)
                .build();

        when(formRepository.findByIdPublicWithSections(formId)).thenReturn(Optional.of(form));
        when(tenantInfoPort.findByTenantId(tenantId)).thenReturn(Optional.empty());

        PublicFormResult result = service.execute(new GetPublicFormQuery(formId));

        assertThat(result.tenantName()).isNull();
        assertThat(result.tenantLogoUrl()).isNull();
        assertThat(result.tenantPrimaryColor()).isNull();
    }

    @Test
    void formNotActive_throwsNotFound() {
        Form form = Form.builder()
                .id(formId).tenantId(tenantId)
                .name("Borrador").type(FormType.CANDIDATES)
                .status(FormStatus.DRAFT)
                .build();

        when(formRepository.findByIdPublicWithSections(formId)).thenReturn(Optional.of(form));

        assertThatThrownBy(() -> service.execute(new GetPublicFormQuery(formId)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void stripsOptionScoresFromScoredQuestionsBeforeReturningThemPublicly() {
        AnswerOption highValueOption = AnswerOption.builder().id("a").label("Mas de 5 anios").score(10).build();
        AnswerOption lowValueOption = AnswerOption.builder().id("b").label("Sin experiencia").score(0).build();
        SingleConfig config = SingleConfig.builder().options(List.of(highValueOption, lowValueOption)).build();
        FormQuestion question = FormQuestion.builder()
                .id(UUID.randomUUID()).title("Anios de experiencia").type(QuestionType.SINGLE)
                .position(0).config(config).build();
        FormSection section = FormSection.builder()
                .id(UUID.randomUUID()).title("Experiencia").position(0)
                .questions(List.of(question)).build();
        Form form = Form.builder()
                .id(formId).tenantId(tenantId)
                .name("Evaluacion").type(FormType.CANDIDATES).status(FormStatus.ACTIVE)
                .sections(List.of(section))
                .build();

        when(formRepository.findByIdPublicWithSections(formId)).thenReturn(Optional.of(form));
        when(tenantInfoPort.findByTenantId(tenantId)).thenReturn(Optional.empty());

        PublicFormResult result = service.execute(new GetPublicFormQuery(formId));

        SingleConfig publicConfig = (SingleConfig) result.sections().get(0).questions().get(0).config();
        assertThat(publicConfig.getOptions()).extracting(AnswerOption::getScore).containsOnly(0);
        assertThat(publicConfig.getOptions()).extracting(AnswerOption::getLabel)
                .containsExactly("Mas de 5 anios", "Sin experiencia");
    }

    @Test
    void formNotFound_throwsNotFound() {
        when(formRepository.findByIdPublicWithSections(formId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetPublicFormQuery(formId)))
                .isInstanceOf(BusinessException.class);
    }
}
