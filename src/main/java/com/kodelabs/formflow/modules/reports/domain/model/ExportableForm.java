package com.kodelabs.formflow.modules.reports.domain.model;

import java.util.List;

public record ExportableForm(String formName, List<ExportableQuestion> questions) {}
