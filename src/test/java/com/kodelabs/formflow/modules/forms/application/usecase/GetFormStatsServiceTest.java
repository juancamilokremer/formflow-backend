package com.kodelabs.formflow.modules.forms.application.usecase;

import com.kodelabs.formflow.modules.forms.application.service.stats.QuestionStatsCalculator;
import com.kodelabs.formflow.modules.forms.application.service.stats.QuestionStatsRegistry;
import com.kodelabs.formflow.modules.forms.application.usecase.form.GetFormStatsService;
import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.FormSnapshot;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.QuestionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.model.snapshot.SectionSnapshot;
import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormStatsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormStatsResult;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.OptionDistribution;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormRepositoryPort;
import com.kodelabs.formflow.modules.forms.domain.port.out.FormResponseRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFormStatsServiceTest {

    @Mock private FormRepositoryPort formRepository;
    @Mock private FormResponseRepositoryPort responseRepository;
    @Mock private QuestionStatsRegistry statsRegistry;
    @InjectMocks private GetFormStatsService service;

    private static final QuestionType SINGLE = QuestionType.SINGLE;

    private UUID formId;
    private UUID tenantId;
    private UUID questionId;
    private Form form;
    private FormQuestion question;

    @BeforeEach
    void setUp() {
        formId     = UUID.randomUUID();
        tenantId   = UUID.randomUUID();
        questionId = UUID.randomUUID();

        question = FormQuestion.builder()
                .id(questionId)
                .title("Tipo de cargo")
                .type(SINGLE)
                .position(0)
                .build();

        FormSection section = FormSection.builder()
                .id(UUID.randomUUID())
                .position(0)
                .questions(List.of(question))
                .build();

        form = Form.builder()
                .id(formId)
                .tenantId(tenantId)
                .name("Evaluación")
                .type(FormType.CANDIDATES)
                .version(1)
                .sections(List.of(section))
                .build();
    }

    @Test
    void returnsStatsAggregatedFromAllResponses() {
        UUID opt1Id = UUID.randomUUID();
        AnswerValue answer1 = AnswerValue.builder().questionId(questionId).value(opt1Id.toString()).build();
        AnswerValue answer2 = AnswerValue.builder().questionId(questionId).value(opt1Id.toString()).build();

        FormResponse r1 = FormResponse.builder().id(UUID.randomUUID()).formId(formId)
                .answers(List.of(answer1)).build();
        FormResponse r2 = FormResponse.builder().id(UUID.randomUUID()).formId(formId)
                .answers(List.of(answer2)).build();

        QuestionStatsResult questionStats = new QuestionStatsResult(
                questionId, "Tipo de cargo", "single", 2, 2,
                List.of(new OptionDistribution(opt1Id.toString(), "Desarrollador", 2, 100.0)),
                null, null, null, null, List.of());

        QuestionStatsCalculator mockCalc = mockCalculator(SINGLE, questionStats);

        when(formRepository.findByIdAndTenantIdWithSections(formId, tenantId)).thenReturn(Optional.of(form));
        when(responseRepository.findAllByFormIdAndTenantId(formId, tenantId, null, null)).thenReturn(List.of(r1, r2));
        when(statsRegistry.find(SINGLE)).thenReturn(Optional.of(mockCalc));

        FormStatsResult result = service.execute(new GetFormStatsQuery(formId, tenantId, null, null));

        assertThat(result.formId()).isEqualTo(formId);
        assertThat(result.totalResponses()).isEqualTo(2);
        assertThat(result.questions()).hasSize(1);
        assertThat(result.questions().get(0).answeredCount()).isEqualTo(2);
        assertThat(result.questions().get(0).distributions()).hasSize(1);
        assertThat(result.questions().get(0).distributions().get(0).count()).isEqualTo(2);
    }

    @Test
    void returnsEmptyStatsWhenNoResponses() {
        QuestionStatsResult emptyStats = new QuestionStatsResult(
                questionId, "Tipo de cargo", "single", 0, 0, List.of(), null, null, null, null, List.of());

        QuestionStatsCalculator mockCalc = mockCalculator(SINGLE, emptyStats);

        when(formRepository.findByIdAndTenantIdWithSections(formId, tenantId)).thenReturn(Optional.of(form));
        when(responseRepository.findAllByFormIdAndTenantId(formId, tenantId, null, null)).thenReturn(List.of());
        when(statsRegistry.find(SINGLE)).thenReturn(Optional.of(mockCalc));

        FormStatsResult result = service.execute(new GetFormStatsQuery(formId, tenantId, null, null));

        assertThat(result.totalResponses()).isZero();
        assertThat(result.questions()).hasSize(1);
        assertThat(result.questions().get(0).answeredCount()).isZero();
    }

    @Test
    void skipsQuestionsWithNoRegisteredCalculator() {
        when(formRepository.findByIdAndTenantIdWithSections(formId, tenantId)).thenReturn(Optional.of(form));
        when(responseRepository.findAllByFormIdAndTenantId(formId, tenantId, null, null)).thenReturn(List.of());
        when(statsRegistry.find(SINGLE)).thenReturn(Optional.empty());

        FormStatsResult result = service.execute(new GetFormStatsQuery(formId, tenantId, null, null));

        assertThat(result.questions()).isEmpty();
    }

    @Test
    void completionRate_averagesAnsweredOverTotalQuestionsInResponseSnapshot() {
        FormSnapshot twoQuestionSnapshot = snapshotWithQuestionCount(2);

        FormResponse fullyAnswered = FormResponse.builder().id(UUID.randomUUID()).formId(formId)
                .formSnapshot(twoQuestionSnapshot)
                .answers(List.of(
                        AnswerValue.builder().questionId(UUID.randomUUID()).value("a").build(),
                        AnswerValue.builder().questionId(UUID.randomUUID()).value("b").build()))
                .build();
        FormResponse halfAnswered = FormResponse.builder().id(UUID.randomUUID()).formId(formId)
                .formSnapshot(twoQuestionSnapshot)
                .answers(List.of(AnswerValue.builder().questionId(UUID.randomUUID()).value("a").build()))
                .build();

        when(formRepository.findByIdAndTenantIdWithSections(formId, tenantId)).thenReturn(Optional.of(form));
        when(responseRepository.findAllByFormIdAndTenantId(formId, tenantId, null, null))
                .thenReturn(List.of(fullyAnswered, halfAnswered));
        when(statsRegistry.find(SINGLE)).thenReturn(Optional.empty());

        FormStatsResult result = service.execute(new GetFormStatsQuery(formId, tenantId, null, null));

        assertThat(result.completionRate()).isEqualTo(0.75); // (1.0 + 0.5) / 2
    }

    @Test
    void completionRate_isNullWhenThereAreNoResponses() {
        when(formRepository.findByIdAndTenantIdWithSections(formId, tenantId)).thenReturn(Optional.of(form));
        when(responseRepository.findAllByFormIdAndTenantId(formId, tenantId, null, null)).thenReturn(List.of());
        when(statsRegistry.find(SINGLE)).thenReturn(Optional.empty());

        FormStatsResult result = service.execute(new GetFormStatsQuery(formId, tenantId, null, null));

        assertThat(result.completionRate()).isNull();
    }

    @Test
    void avgResponseTimeSeconds_averagesOnlyResponsesThatHaveStartedAt() {
        Instant base = Instant.parse("2026-08-01T10:00:00Z");
        FormResponse withTiming = FormResponse.builder().id(UUID.randomUUID()).formId(formId)
                .startedAt(base).submittedAt(base.plusSeconds(120))
                .answers(List.of())
                .build();
        FormResponse withoutStartedAt = FormResponse.builder().id(UUID.randomUUID()).formId(formId)
                .submittedAt(base.plusSeconds(500))
                .answers(List.of())
                .build();

        when(formRepository.findByIdAndTenantIdWithSections(formId, tenantId)).thenReturn(Optional.of(form));
        when(responseRepository.findAllByFormIdAndTenantId(formId, tenantId, null, null))
                .thenReturn(List.of(withTiming, withoutStartedAt));
        when(statsRegistry.find(SINGLE)).thenReturn(Optional.empty());

        FormStatsResult result = service.execute(new GetFormStatsQuery(formId, tenantId, null, null));

        assertThat(result.avgResponseTimeSeconds()).isEqualTo(120L);
    }

    @Test
    void avgResponseTimeSeconds_isNullWhenNoResponseHasStartedAt() {
        FormResponse withoutStartedAt = FormResponse.builder().id(UUID.randomUUID()).formId(formId)
                .submittedAt(Instant.now()).answers(List.of()).build();

        when(formRepository.findByIdAndTenantIdWithSections(formId, tenantId)).thenReturn(Optional.of(form));
        when(responseRepository.findAllByFormIdAndTenantId(formId, tenantId, null, null)).thenReturn(List.of(withoutStartedAt));
        when(statsRegistry.find(SINGLE)).thenReturn(Optional.empty());

        FormStatsResult result = service.execute(new GetFormStatsQuery(formId, tenantId, null, null));

        assertThat(result.avgResponseTimeSeconds()).isNull();
    }

    @Test
    void timeline_groupsResponsesByUtcSubmissionDate() {
        FormResponse dayOneFirst = FormResponse.builder().id(UUID.randomUUID()).formId(formId)
                .submittedAt(Instant.parse("2026-08-01T08:00:00Z")).answers(List.of()).build();
        FormResponse dayOneSecond = FormResponse.builder().id(UUID.randomUUID()).formId(formId)
                .submittedAt(Instant.parse("2026-08-01T22:00:00Z")).answers(List.of()).build();
        FormResponse dayTwo = FormResponse.builder().id(UUID.randomUUID()).formId(formId)
                .submittedAt(Instant.parse("2026-08-02T09:00:00Z")).answers(List.of()).build();

        when(formRepository.findByIdAndTenantIdWithSections(formId, tenantId)).thenReturn(Optional.of(form));
        when(responseRepository.findAllByFormIdAndTenantId(formId, tenantId, null, null))
                .thenReturn(List.of(dayOneFirst, dayOneSecond, dayTwo));
        when(statsRegistry.find(SINGLE)).thenReturn(Optional.empty());

        FormStatsResult result = service.execute(new GetFormStatsQuery(formId, tenantId, null, null));

        assertThat(result.timeline()).hasSize(2);
        assertThat(result.timeline().get(0).date()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(result.timeline().get(0).count()).isEqualTo(2);
        assertThat(result.timeline().get(1).date()).isEqualTo(LocalDate.of(2026, 8, 2));
        assertThat(result.timeline().get(1).count()).isEqualTo(1);
    }

    private FormSnapshot snapshotWithQuestionCount(int count) {
        List<QuestionSnapshot> questions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            questions.add(new QuestionSnapshot(
                    UUID.randomUUID(), "Q" + i, null, "single", i, true, null, null, null));
        }
        SectionSnapshot section = new SectionSnapshot(UUID.randomUUID(), "Sección", null, 0, null, questions);
        return new FormSnapshot(formId, "Evaluación", "candidates", 1, Instant.now(), List.of(section));
    }

    @Test
    void passesTheRequestedDateRangeThroughToTheRepository() {
        Instant from = Instant.parse("2026-08-01T00:00:00Z");
        Instant to = Instant.parse("2026-08-07T23:59:59Z");

        when(formRepository.findByIdAndTenantIdWithSections(formId, tenantId)).thenReturn(Optional.of(form));
        when(responseRepository.findAllByFormIdAndTenantId(formId, tenantId, from, to)).thenReturn(List.of());
        when(statsRegistry.find(SINGLE)).thenReturn(Optional.empty());

        FormStatsResult result = service.execute(new GetFormStatsQuery(formId, tenantId, from, to));

        assertThat(result.totalResponses()).isZero();
    }

    @Test
    void throwsNotFoundWhenFormDoesNotBelongToTenant() {
        when(formRepository.findByIdAndTenantIdWithSections(formId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetFormStatsQuery(formId, tenantId, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private QuestionStatsCalculator mockCalculator(QuestionType questionType, QuestionStatsResult result) {
        return new QuestionStatsCalculator() {
            @Override
            public QuestionType type() { return questionType; }

            @Override
            public QuestionStatsResult calculate(FormQuestion question, int totalResponses, List<Object> answers) {
                return result;
            }
        };
    }
}
