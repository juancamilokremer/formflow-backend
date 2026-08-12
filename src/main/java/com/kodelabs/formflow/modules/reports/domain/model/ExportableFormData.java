package com.kodelabs.formflow.modules.reports.domain.model;

import java.util.List;

public record ExportableFormData(ExportableForm form, List<ExportableResponse> responses) {}
