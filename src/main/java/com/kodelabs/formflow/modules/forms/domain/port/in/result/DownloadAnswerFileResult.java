package com.kodelabs.formflow.modules.forms.domain.port.in.result;

public record DownloadAnswerFileResult(String filename, String contentType, byte[] content) {}
