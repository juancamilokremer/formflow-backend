package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormStatsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormStatsResult;

public interface GetFormStatsUseCase {
    FormStatsResult execute(GetFormStatsQuery query);
}
