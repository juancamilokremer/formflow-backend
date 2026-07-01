package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.MatrixRowStats;

import java.util.List;

public record MatrixRowStatsResponse(
        String rowId,
        String rowLabel,
        List<MatrixCellStatsResponse> cells
) {
    public static MatrixRowStatsResponse from(MatrixRowStats r) {
        List<MatrixCellStatsResponse> cells = r.cells().stream()
                .map(MatrixCellStatsResponse::from).toList();
        return new MatrixRowStatsResponse(r.rowId(), r.rowLabel(), cells);
    }
}
