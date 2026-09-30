package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetInvitationQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.InvitationPreviewResult;

public interface GetInvitationUseCase {
    InvitationPreviewResult execute(GetInvitationQuery query);
}
