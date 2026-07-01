package com.kodelabs.formflow.modules.forms.domain.port.in.result;

public record OptionDistribution(
        String optionId,
        String label,
        int count,
        double percentage
) {}
