package com.kodelabs.formflow.modules.forms.application.service.stats;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.AnswerOption;
import com.kodelabs.formflow.modules.forms.domain.model.config.SingleConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.OptionDistribution;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SingleStatsCalculator implements QuestionStatsCalculator {

    public static final QuestionType QUESTION_TYPE = new QuestionType("single");

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public QuestionStatsResult calculate(FormQuestion question, int totalResponses, List<Object> answers) {
        List<AnswerOption> options = question.getConfig() instanceof SingleConfig cfg
                ? cfg.getOptions()
                : List.of();

        Map<String, Long> counts = answers.stream()
                .filter(a -> a instanceof String)
                .collect(Collectors.groupingBy(a -> (String) a, Collectors.counting()));

        int answered = (int) answers.stream().filter(a -> a instanceof String s && !s.isBlank()).count();

        List<OptionDistribution> distributions = options.stream()
                .map(opt -> new OptionDistribution(
                        opt.getId(),
                        opt.getLabel(),
                        counts.getOrDefault(opt.getId(), 0L).intValue(),
                        answered == 0 ? 0.0 : counts.getOrDefault(opt.getId(), 0L) * 100.0 / answered))
                .toList();

        return new QuestionStatsResult(
                question.getId(), question.getTitle(), type().code(),
                totalResponses, answered, distributions, null, null, null, null);
    }
}
