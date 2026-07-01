package com.kodelabs.formflow.modules.forms.application.service.stats;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DateStatsCalculator implements QuestionStatsCalculator {

    public static final QuestionType QUESTION_TYPE = QuestionType.DATE;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public QuestionStatsResult calculate(FormQuestion question, int totalResponses, List<Object> answers) {
        int answered = (int) answers.stream()
                .filter(a -> a instanceof String s && !s.isBlank())
                .count();

        return new QuestionStatsResult(
                question.getId(), question.getTitle(), type().code(),
                totalResponses, answered, null, null, null, null, null);
    }
}
