package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdateBrandingCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;

public interface UpdateBrandingUseCase {
    BrandingResult execute(UpdateBrandingCommand command);
}
