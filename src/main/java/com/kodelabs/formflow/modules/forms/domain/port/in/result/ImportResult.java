package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.List;

public record ImportResult(int imported, int skipped, List<String> errors) {}
