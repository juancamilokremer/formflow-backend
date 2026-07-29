package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetCandidatePortalQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.CandidatePortalResult;

public interface GetCandidatePortalUseCase {
    CandidatePortalResult execute(GetCandidatePortalQuery query);
}
