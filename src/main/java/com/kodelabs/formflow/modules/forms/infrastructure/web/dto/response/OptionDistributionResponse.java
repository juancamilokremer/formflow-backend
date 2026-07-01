package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.OptionDistribution;

public record OptionDistributionResponse(
        String optionId,
        String label,
        int count,
        double percentage
) {
    public static OptionDistributionResponse from(OptionDistribution d) {
        return new OptionDistributionResponse(d.optionId(), d.label(), d.count(), d.percentage());
    }
}
