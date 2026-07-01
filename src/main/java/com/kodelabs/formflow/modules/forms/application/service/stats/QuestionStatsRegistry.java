package com.kodelabs.formflow.modules.forms.application.service.stats;

import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class QuestionStatsRegistry {

    private final Map<QuestionType, QuestionStatsCalculator> calculators;

    public QuestionStatsRegistry(List<QuestionStatsCalculator> calculatorList) {
        Map<QuestionType, QuestionStatsCalculator> map = new LinkedHashMap<>();
        calculatorList.stream()
                .sorted((a, b) -> a.type().code().compareTo(b.type().code()))
                .forEach(calc -> {
                    if (map.containsKey(calc.type())) {
                        throw new IllegalStateException("Duplicate QuestionStatsCalculator type: " + calc.type().code());
                    }
                    map.put(calc.type(), calc);
                });
        this.calculators = Collections.unmodifiableMap(map);
    }

    public Optional<QuestionStatsCalculator> find(QuestionType type) {
        return Optional.ofNullable(calculators.get(type));
    }
}
