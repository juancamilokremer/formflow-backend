package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.util.UUID;

public record UploadAnswerFileResult(UUID fileId, String filename) {}
