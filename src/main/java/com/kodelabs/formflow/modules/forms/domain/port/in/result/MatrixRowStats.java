package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.List;

public record MatrixRowStats(
        String rowId,
        String rowLabel,
        List<MatrixCellStats> cells
) {}
