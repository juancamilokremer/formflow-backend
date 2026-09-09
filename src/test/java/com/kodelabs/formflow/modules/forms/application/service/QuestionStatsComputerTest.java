package com.kodelabs.formflow.modules.forms.application.service;

import com.kodelabs.formflow.modules.forms.application.service.stats.QuestionStatsCalculator;
import com.kodelabs.formflow.modules.forms.application.service.stats.QuestionStatsRegistry;
import com.kodelabs.formflow.modules.forms.domain.model.AnswerValue;
import com.kodelabs.formflow.modules.forms.domain.model.Form;
import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.FormResponse;
import com.kodelabs.formflow.modules.forms.domain.model.FormSection;
import com.kodelabs.formflow.modules.forms.domain.model.FormType;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionStatsComputerTest {

    @Mock private QuestionStatsRegistry statsRegistry;
    @InjectMocks private QuestionStatsComputer computer;

    @Test
    void groupsAnswerValuesByQuestionAndPassesThemToItsCalculator() {
        UUID formId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        FormQuestion question = FormQuestion.builder()
                .id(questionId).title("¿Años de experiencia?").type(QuestionType.SINGLE).position(0).build();
        Form form = formWith(formId, section(0, question));

        FormResponse r1 = FormResponse.builder().id(UUID.randomUUID()).formId(formId)
                .answers(List.of(AnswerValue.builder().questionId(questionId).value("3-5").build())).build();
        FormResponse r2 = FormResponse.builder().id(UUID.randomUUID()).formId(formId)
                .answers(List.of(AnswerValue.builder().questionId(questionId).value("3-5").build())).build();

        RecordingCalculator recordingCalc = new RecordingCalculator(QuestionType.SINGLE);
        when(statsRegistry.find(QuestionType.SINGLE)).thenReturn(Optional.of(recordingCalc));

        computer.compute(form, 2, List.of(r1, r2));

        assertThat(recordingCalc.receivedTotalResponses).isEqualTo(2);
        assertThat(recordingCalc.receivedAnswers).containsExactly("3-5", "3-5");
    }

    @Test
    void ignoresAnswersWithNullValueWhenGrouping() {
        UUID formId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        FormQuestion question = FormQuestion.builder()
                .id(questionId).title("Comentario").type(QuestionType.TEXT).position(0).build();
        Form form = formWith(formId, section(0, question));

        FormResponse withoutAnswer = FormResponse.builder().id(UUID.randomUUID()).formId(formId)
                .answers(List.of(AnswerValue.builder().questionId(questionId).value(null).build())).build();

        RecordingCalculator recordingCalc = new RecordingCalculator(QuestionType.TEXT);
        when(statsRegistry.find(QuestionType.TEXT)).thenReturn(Optional.of(recordingCalc));

        computer.compute(form, 1, List.of(withoutAnswer));

        assertThat(recordingCalc.receivedAnswers).isEmpty();
    }

    @Test
    void ordersQuestionsBySectionPositionThenQuestionPosition() {
        UUID formId = UUID.randomUUID();
        FormQuestion secondSectionQuestion = FormQuestion.builder()
                .id(UUID.randomUUID()).title("B").type(QuestionType.TEXT).position(0).build();
        FormQuestion firstSectionSecondQuestion = FormQuestion.builder()
                .id(UUID.randomUUID()).title("A2").type(QuestionType.TEXT).position(1).build();
        FormQuestion firstSectionFirstQuestion = FormQuestion.builder()
                .id(UUID.randomUUID()).title("A1").type(QuestionType.TEXT).position(0).build();

        Form form = Form.builder()
                .id(formId).tenantId(UUID.randomUUID()).name("Form").type(FormType.CANDIDATES).version(1)
                .sections(List.of(
                        section(1, secondSectionQuestion),
                        section(0, firstSectionFirstQuestion, firstSectionSecondQuestion)))
                .build();

        OrderRecordingCalculator recordingCalc = new OrderRecordingCalculator(QuestionType.TEXT);
        when(statsRegistry.find(QuestionType.TEXT)).thenReturn(Optional.of(recordingCalc));

        computer.compute(form, 0, List.of());

        assertThat(recordingCalc.receivedTitlesInOrder).containsExactly("A1", "A2", "B");
    }

    @Test
    void skipsQuestionsWithNoRegisteredCalculator() {
        UUID formId = UUID.randomUUID();
        FormQuestion question = FormQuestion.builder()
                .id(UUID.randomUUID()).title("Firma") .type(QuestionType.FILE).position(0).build();
        Form form = formWith(formId, section(0, question));

        when(statsRegistry.find(QuestionType.FILE)).thenReturn(Optional.empty());

        List<QuestionStatsResult> result = computer.compute(form, 0, List.of());

        assertThat(result).isEmpty();
    }

    private Form formWith(UUID formId, FormSection... sections) {
        return Form.builder()
                .id(formId).tenantId(UUID.randomUUID()).name("Form")
                .type(FormType.CANDIDATES).version(1)
                .sections(List.of(sections))
                .build();
    }

    private FormSection section(int position, FormQuestion... questions) {
        return FormSection.builder().id(UUID.randomUUID()).position(position).questions(List.of(questions)).build();
    }

    private static class RecordingCalculator implements QuestionStatsCalculator {
        private final QuestionType type;
        private int receivedTotalResponses;
        private List<Object> receivedAnswers;

        RecordingCalculator(QuestionType type) { this.type = type; }

        @Override
        public QuestionType type() { return type; }

        @Override
        public QuestionStatsResult calculate(FormQuestion question, int totalResponses, List<Object> answers) {
            this.receivedTotalResponses = totalResponses;
            this.receivedAnswers = answers;
            return new QuestionStatsResult(
                    question.getId(), question.getTitle(), type.name().toLowerCase(),
                    totalResponses, answers.size(), List.of(), null, null, null, null, List.of());
        }
    }

    private static class OrderRecordingCalculator implements QuestionStatsCalculator {
        private final QuestionType type;
        private final List<String> receivedTitlesInOrder = new java.util.ArrayList<>();

        OrderRecordingCalculator(QuestionType type) { this.type = type; }

        @Override
        public QuestionType type() { return type; }

        @Override
        public QuestionStatsResult calculate(FormQuestion question, int totalResponses, List<Object> answers) {
            receivedTitlesInOrder.add(question.getTitle());
            return new QuestionStatsResult(
                    question.getId(), question.getTitle(), type.name().toLowerCase(),
                    totalResponses, 0, List.of(), null, null, null, null, List.of());
        }
    }
}
