package com.kodelabs.formflow.modules.forms.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.model.config.DateConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.FileConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.MatrixConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.MultipleConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.NpsConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.QuestionConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.ScaleConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.ScoringType;
import com.kodelabs.formflow.modules.forms.domain.model.config.SingleConfig;
import com.kodelabs.formflow.modules.forms.domain.model.config.TextConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class QuestionConfigFactory {

    private final ObjectMapper objectMapper;

    public QuestionConfig build(QuestionType type, Map<String, Object> raw) {
        if (raw == null) raw = Map.of();
        QuestionConfig config = switch (type) {
            case TEXT -> objectMapper.convertValue(raw, TextConfig.class);
            case SINGLE -> objectMapper.convertValue(raw, SingleConfig.class);
            case MULTIPLE -> objectMapper.convertValue(raw, MultipleConfig.class);
            case SCALE -> buildScale(raw);
            case DATE -> objectMapper.convertValue(raw, DateConfig.class);
            case FILE -> objectMapper.convertValue(raw, FileConfig.class);
            case MATRIX -> objectMapper.convertValue(raw, MatrixConfig.class);
            case NPS -> objectMapper.convertValue(raw, NpsConfig.class);
        };
        config.validate();
        return config;
    }

    private ScaleConfig buildScale(Map<String, Object> raw) {
        ScaleConfig config = objectMapper.convertValue(raw, ScaleConfig.class);
        if (config.getScoringType() == ScoringType.AUTO) {
            config.calculateAutoScores();
        }
        return config;
    }
}
