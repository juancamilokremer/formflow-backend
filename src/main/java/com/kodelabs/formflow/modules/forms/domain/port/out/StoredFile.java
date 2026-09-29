package com.kodelabs.formflow.modules.forms.domain.port.out;

public record StoredFile(String filename, String contentType, byte[] content) {}
