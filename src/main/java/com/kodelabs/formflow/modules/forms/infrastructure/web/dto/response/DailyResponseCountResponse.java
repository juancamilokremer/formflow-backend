package com.kodelabs.formflow.modules.forms.infrastructure.web.dto.response;

import com.kodelabs.formflow.modules.forms.domain.port.in.result.DailyResponseCountResult;

import java.time.LocalDate;

public record DailyResponseCountResponse(LocalDate date, int count) {

    public static DailyResponseCountResponse from(DailyResponseCountResult r) {
        return new DailyResponseCountResponse(r.date(), r.count());
    }
}
