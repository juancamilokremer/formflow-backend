package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.UploadLogoCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;

public interface UploadLogoUseCase {
    BrandingResult execute(UploadLogoCommand command);
}
