package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.ListConvocatoriasQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaSummaryResult;

import java.util.List;

public interface ListConvocatoriasUseCase {
    List<ConvocatoriaSummaryResult> execute(ListConvocatoriasQuery query);
}
