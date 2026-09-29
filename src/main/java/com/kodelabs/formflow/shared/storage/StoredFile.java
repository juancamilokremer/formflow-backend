package com.kodelabs.formflow.shared.storage;

public record StoredFile(String filename, String contentType, byte[] content) {}
