package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import com.kodelabs.formflow.modules.forms.domain.model.Form;

/**
 * Wraps a Form with its sections already populated.
 */
public record FormDetailResult(Form form) {}
