package com.kodelabs.formflow.modules.auth.application.usecase.me;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.ChangeMyPasswordUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ChangeMyPasswordCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The logged-in user changes their own password directly (current + new), unlike the
 *  forgot/reset-by-email flow — this requires an active session, so there is no need to
 *  prove ownership of the inbox again, only to re-confirm the current password. */
@Service
@RequiredArgsConstructor
public class ChangeMyPasswordService implements ChangeMyPasswordUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;

    @Override
    @Transactional
    public void execute(ChangeMyPasswordCommand command) {
        User user = userRepository.findByIdAndTenantId(command.userId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.user.not_found", HttpStatus.NOT_FOUND));

        if (!passwordHasher.matches(command.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("error.auth.invalid_current_password", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordHasher.hash(command.newPassword()));
        userRepository.save(user);
    }
}
