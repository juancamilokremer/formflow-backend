package com.kodelabs.formflow.modules.forms.application.service.stats;

import com.kodelabs.formflow.modules.forms.domain.model.FormQuestion;
import com.kodelabs.formflow.modules.forms.domain.model.QuestionType;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.QuestionStatsResult;

import java.util.List;

public interface QuestionStatsCalculator {

    QuestionType type();

    QuestionStatsResult calculate(FormQuestion question, int totalResponses, List<Object> answers);
}
