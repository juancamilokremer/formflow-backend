package com.kodelabs.formflow.modules.forms.domain.port.in.result;

import java.time.LocalDate;

public record DailyResponseCountResult(LocalDate date, int count) {}
