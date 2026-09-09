package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetConvocatoriaQuestionStatsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaQuestionStatsResult;

public interface GetConvocatoriaQuestionStatsUseCase {
    ConvocatoriaQuestionStatsResult execute(GetConvocatoriaQuestionStatsQuery query);
}
