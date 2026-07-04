package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetPublicCandidateFormQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.PublicCandidateFormResult;

public interface GetPublicCandidateFormUseCase {
    PublicCandidateFormResult execute(GetPublicCandidateFormQuery query);
}
