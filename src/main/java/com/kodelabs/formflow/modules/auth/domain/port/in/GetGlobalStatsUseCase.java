package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.result.GlobalStatsResult;

public interface GetGlobalStatsUseCase {
    GlobalStatsResult execute();
}
