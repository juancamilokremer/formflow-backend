package com.kodelabs.formflow.modules.forms.application.service.stats;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TextStatsCalculator implements QuestionStatsCalculator {

    public static final QuestionType QUESTION_TYPE = QuestionType.TEXT;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    private static final int SAMPLE_SIZE = 3;
    private static final int SAMPLE_MAX_LENGTH = 100;

    @Override
    public QuestionStatsResult calculate(FormQuestion question, int totalResponses, List<Object> answers) {
        List<String> nonBlankAnswers = answers.stream()
                .filter(a -> a instanceof String s && !s.isBlank())
                .map(a -> (String) a)
                .toList();

        List<String> sampleAnswers = nonBlankAnswers.stream()
                .limit(SAMPLE_SIZE)
                .map(this::truncate)
                .toList();

        return new QuestionStatsResult(
                question.getId(), question.getTitle(), type().code(),
                totalResponses, nonBlankAnswers.size(), null, null, null, null, null, sampleAnswers);
    }

    private String truncate(String answer) {
        return answer.length() <= SAMPLE_MAX_LENGTH ? answer : answer.substring(0, SAMPLE_MAX_LENGTH);
    }
}
