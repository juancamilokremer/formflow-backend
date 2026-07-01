package com.kodelabs.formflow.modules.forms.application.service.stats;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.ScaleConfig;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.OptionDistribution;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScaleStatsCalculator implements QuestionStatsCalculator {

    public static final QuestionType QUESTION_TYPE = QuestionType.SCALE;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public QuestionStatsResult calculate(FormQuestion question, int totalResponses, List<Object> answers) {
        int min = 1;
        int max = 5;
        if (question.getConfig() instanceof ScaleConfig cfg) {
            min = cfg.getMin();
            max = cfg.getMax();
        }

        List<Integer> values = answers.stream()
                .filter(a -> a instanceof Number)
                .map(a -> ((Number) a).intValue())
                .collect(Collectors.toList());

        int answered = values.size();
        Double average = answered == 0 ? null : values.stream().mapToInt(Integer::intValue).average().orElse(0);
        Double median  = answered == 0 ? null : computeMedian(values);

        final int minFinal = min;
        final int maxFinal = max;
        List<OptionDistribution> distributions = new ArrayList<>();
        for (int v = minFinal; v <= maxFinal; v++) {
            final int value = v;
            int count = (int) values.stream().filter(x -> x == value).count();
            distributions.add(new OptionDistribution(
                    String.valueOf(v), String.valueOf(v), count,
                    answered == 0 ? 0.0 : count * 100.0 / answered));
        }

        return new QuestionStatsResult(
                question.getId(), question.getTitle(), type().code(),
                totalResponses, answered, distributions, average, median, null, null);
    }

    private double computeMedian(List<Integer> values) {
        List<Integer> sorted = values.stream().sorted().toList();
        int n = sorted.size();
        if (n % 2 == 0) {
            return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
        }
        return sorted.get(n / 2);
    }
}
