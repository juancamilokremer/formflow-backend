package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetPublicBrandingQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;

public interface GetPublicBrandingUseCase {
    BrandingResult execute(GetPublicBrandingQuery query);
}
