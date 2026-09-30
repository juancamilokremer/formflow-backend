package com.kodelabs.formflow.modules.auth.application.usecase.user;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.in.ChangeUserRoleUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ChangeUserRoleCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.UserResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeUserRoleService implements ChangeUserRoleUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    @Transactional
    public UserResult execute(ChangeUserRoleCommand command) {
        User user = userRepository.findByIdAndTenantId(command.targetUserId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.user.not_found", HttpStatus.NOT_FOUND));

        if (command.newRole() == UserRole.SUPER_ADMIN) {
            throw new BusinessException("error.user.invalid_role", HttpStatus.BAD_REQUEST);
        }
        if (command.newRole() != UserRole.TENANT_ADMIN) {
            LastAdminGuard.assertAnotherAdminRemains(userRepository, user);
        }

        user.setRole(command.newRole());
        return UserResult.from(userRepository.save(user));
    }
}
