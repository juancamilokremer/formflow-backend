package com.kodelabs.formflow.modules.auth.application.usecase.user;

import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.ListUsersUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ListUsersQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.UserResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListUsersService implements ListUsersUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    public List<UserResult> execute(ListUsersQuery query) {
        return userRepository.findAllByTenantIdAndActiveTrueAndRoleNot(query.tenantId(), UserRole.SUPER_ADMIN).stream()
                .map(UserResult::from)
                .toList();
    }
}
