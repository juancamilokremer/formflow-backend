package com.kodelabs.formflow.modules.forms.application.service.stats;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.AnswerOption;
import com.kodelabs.formflow.modules.forms.domain.model.config.MultipleConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.OptionDistribution;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MultipleStatsCalculator implements QuestionStatsCalculator {

    public static final QuestionType QUESTION_TYPE = QuestionType.MULTIPLE;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public QuestionStatsResult calculate(FormQuestion question, int totalResponses, List<Object> answers) {
        List<AnswerOption> options = question.getConfig() instanceof MultipleConfig cfg
                ? cfg.getOptions()
                : List.of();

        Map<String, Integer> counts = new HashMap<>();
        int answered = 0;

        for (Object answer : answers) {
            if (answer instanceof List<?> selected && !selected.isEmpty()) {
                answered++;
                for (Object item : selected) {
                    if (item instanceof String id) {
                        counts.merge(id, 1, Integer::sum);
                    }
                }
            }
        }

        final int answeredFinal = answered;
        List<OptionDistribution> distributions = options.stream()
                .map(opt -> new OptionDistribution(
                        opt.getId(),
                        opt.getLabel(),
                        counts.getOrDefault(opt.getId(), 0),
                        answeredFinal == 0 ? 0.0 : counts.getOrDefault(opt.getId(), 0) * 100.0 / answeredFinal))
                .toList();

        return new QuestionStatsResult(
                question.getId(), question.getTitle(), type().code(),
                totalResponses, answered, distributions, null, null, null, null);
    }
}
