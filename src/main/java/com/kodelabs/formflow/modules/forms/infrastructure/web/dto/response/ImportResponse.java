package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.ImportResult;

import java.util.List;

public record ImportResponse(int imported, int skipped, List<String> errors) {

    public static ImportResponse from(ImportResult r) {
        return new ImportResponse(r.imported(), r.skipped(), r.errors());
    }
}
