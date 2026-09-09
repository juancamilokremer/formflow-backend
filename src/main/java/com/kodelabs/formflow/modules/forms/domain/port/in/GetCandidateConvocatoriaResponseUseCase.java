package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetCandidateConvocatoriaResponseQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidateConvocatoriaResponseResult;

public interface GetCandidateConvocatoriaResponseUseCase {
    CandidateConvocatoriaResponseResult execute(GetCandidateConvocatoriaResponseQuery query);
}
