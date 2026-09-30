package com.kodelabs.formflow.modules.auth.domain.port.in;

import com.kodelabs.formflow.modules.auth.domain.port.in.command.ListUsersQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.UserResult;

import java.util.List;

public interface ListUsersUseCase {
    List<UserResult> execute(ListUsersQuery query);
}
