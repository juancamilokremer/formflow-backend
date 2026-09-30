package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetBrandingQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;

public interface GetBrandingUseCase {
    BrandingResult execute(GetBrandingQuery query);
}
