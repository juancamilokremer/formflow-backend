package com.kodelabs.formflow.modules.forms.domain.model.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScaleConfig extends QuestionConfig implements Validatable {

    @Builder.Default
    private int min = 1;
    @Builder.Default
    private int max = 5;
    private String minLabel;
    private String maxLabel;
    @Builder.Default
    private ScoringType scoringType = ScoringType.NONE;
    @Builder.Default
    private List<ScaleOption> options = new ArrayList<>();

    @Override
    public int maxScore() {
        return options.stream().mapToInt(ScaleOption::getScore).max().orElse(0);
    }

    /** Distributes scores linearly: score = round(value * 10.0 / max). */
    public void calculateAutoScores() {
        options = new ArrayList<>();
        for (int v = min; v <= max; v++) {
            int score = (int) Math.round((double) v * 10.0 / max);
            options.add(new ScaleOption(v, score));
        }
    }

    @Override
    public void validate() {
        require(min, "min");
        require(max, "max");
        if (max <= min) {
            throw new com.kodelabs.formflow.shared.exception.BusinessException(
                    "error.question.scale_range_invalid", org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }
}
