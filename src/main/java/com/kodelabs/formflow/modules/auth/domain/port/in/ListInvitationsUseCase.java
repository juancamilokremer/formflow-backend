package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.ListInvitationsQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.InvitationResult;

import java.util.List;

public interface ListInvitationsUseCase {
    List<InvitationResult> execute(ListInvitationsQuery query);
}
