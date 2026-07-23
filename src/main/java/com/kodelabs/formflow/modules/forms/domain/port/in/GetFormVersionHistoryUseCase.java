package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.GetFormVersionHistoryQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormVersionResult;

import java.util.List;

public interface GetFormVersionHistoryUseCase {
    List<FormVersionResult> execute(GetFormVersionHistoryQuery query);
}
