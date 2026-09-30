package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.DeleteLogoCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;

public interface DeleteLogoUseCase {
    BrandingResult execute(DeleteLogoCommand command);
}
