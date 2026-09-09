package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.ConvocatoriaQuestionStatsResult;

import java.util.List;
import java.util.UUID;

public record ConvocatoriaQuestionStatsResponse(
        UUID convocatoriaId,
        String convocatoriaName,
        List<FormQuestionStatsResponse> forms
) {
    public static ConvocatoriaQuestionStatsResponse from(ConvocatoriaQuestionStatsResult r) {
        List<FormQuestionStatsResponse> forms = r.forms().stream()
                .map(FormQuestionStatsResponse::from).toList();
        return new ConvocatoriaQuestionStatsResponse(r.convocatoriaId(), r.convocatoriaName(), forms);
    }
}
