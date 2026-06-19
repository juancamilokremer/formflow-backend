package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetRankingQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateResult;

import java.util.List;

public interface GetRankingUseCase {
    List<CandidateResult> execute(GetRankingQuery query);
}
