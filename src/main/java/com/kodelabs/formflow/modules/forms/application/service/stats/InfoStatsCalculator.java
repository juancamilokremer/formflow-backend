package com.kodelabs.formflow.modules.forms.application.service.stats;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InfoStatsCalculator implements QuestionStatsCalculator {

    public static final QuestionType QUESTION_TYPE = new QuestionType("info");

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public QuestionStatsResult calculate(FormQuestion question, int totalResponses, List<Object> answers) {
        return new QuestionStatsResult(
                question.getId(), question.getTitle(), type().code(),
                totalResponses, 0, null, null, null, null, null);
    }
}
