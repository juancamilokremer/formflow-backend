package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetConvocatoriaStatsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaStatsResult;

public interface GetConvocatoriaStatsUseCase {
    ConvocatoriaStatsResult execute(GetConvocatoriaStatsQuery query);
}
