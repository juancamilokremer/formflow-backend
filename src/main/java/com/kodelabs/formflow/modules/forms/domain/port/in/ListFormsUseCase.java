package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.ListFormsQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.FormSummaryResult;

import java.util.List;

public interface ListFormsUseCase {
    List<FormSummaryResult> execute(ListFormsQuery query);
}
