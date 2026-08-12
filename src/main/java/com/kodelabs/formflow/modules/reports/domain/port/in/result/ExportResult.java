package com.kodelabs.formflow.modules.reports.domain.port.in.result;

public record ExportResult(byte[] content, String filename, String contentType) {}
