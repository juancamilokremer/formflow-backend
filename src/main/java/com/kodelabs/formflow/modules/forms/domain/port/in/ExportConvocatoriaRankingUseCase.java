package com.kodelabs.formflow.modules.forms.domain.port.in;

import com.kodelabs.formflow.modules.forms.domain.port.in.command.ExportConvocatoriaRankingQuery;
import com.kodelabs.formflow.modules.forms.domain.port.in.result.ExportConvocatoriaRankingResult;

public interface ExportConvocatoriaRankingUseCase {
    ExportConvocatoriaRankingResult execute(ExportConvocatoriaRankingQuery query);
}
