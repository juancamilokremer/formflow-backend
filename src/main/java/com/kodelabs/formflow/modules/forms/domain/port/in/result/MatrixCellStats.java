package com.kodelabs.formflow.modules.forms.domain.port.in.result;

public record MatrixCellStats(
        String columnId,
        String columnLabel,
        int count,
        double percentage
) {}
