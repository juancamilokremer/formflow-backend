package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.MatrixCellStats;

public record MatrixCellStatsResponse(
        String columnId,
        String columnLabel,
        int count,
        double percentage
) {
    public static MatrixCellStatsResponse from(MatrixCellStats c) {
        return new MatrixCellStatsResponse(c.columnId(), c.columnLabel(), c.count(), c.percentage());
    }
}
