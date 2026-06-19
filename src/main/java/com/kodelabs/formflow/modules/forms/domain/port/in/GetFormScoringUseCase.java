package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormScoringQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormScoringResult;

public interface GetFormScoringUseCase {
    FormScoringResult execute(GetFormScoringQuery query);
}
