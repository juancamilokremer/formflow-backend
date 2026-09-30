package com.kodelabs.formflow.modules.auth.application.usecase.user;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.RevokeUserAccessUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.RevokeUserAccessCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RevokeUserAccessService implements RevokeUserAccessUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    @Transactional
    public void execute(RevokeUserAccessCommand command) {
        if (command.targetUserId().equals(command.actingUserId())) {
            throw new BusinessException("error.user.cannot_revoke_self", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByIdAndTenantId(command.targetUserId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.user.not_found", HttpStatus.NOT_FOUND));

        LastAdminGuard.assertAnotherAdminRemains(userRepository, user);

        user.setActive(false);
        userRepository.save(user);
    }
}
