package com.kodelabs.formflow.modules.forms.application.service.stats;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.OptionDistribution;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NpsStatsCalculator implements QuestionStatsCalculator {

    public static final QuestionType QUESTION_TYPE = QuestionType.NPS;

    @Override
    public QuestionType type() {
        return QUESTION_TYPE;
    }

    @Override
    public QuestionStatsResult calculate(FormQuestion question, int totalResponses, List<Object> answers) {
        List<Integer> values = answers.stream()
                .filter(a -> a instanceof Number)
                .map(a -> ((Number) a).intValue())
                .collect(Collectors.toList());

        int answered = values.size();
        Double average   = answered == 0 ? null : values.stream().mapToInt(Integer::intValue).average().orElse(0);
        long promoters   = values.stream().filter(v -> v >= 9).count();
        long detractors  = values.stream().filter(v -> v <= 6).count();
        Double npsScore  = answered == 0 ? null : (promoters - detractors) * 100.0 / answered;

        List<OptionDistribution> distributions = new ArrayList<>();
        for (int v = 0; v <= 10; v++) {
            final int value = v;
            int count = (int) values.stream().filter(x -> x == value).count();
            distributions.add(new OptionDistribution(
                    String.valueOf(v), String.valueOf(v), count,
                    answered == 0 ? 0.0 : count * 100.0 / answered));
        }

        return new QuestionStatsResult(
                question.getId(), question.getTitle(), type().code(),
                totalResponses, answered, distributions, average, null, npsScore, null);
    }
}
