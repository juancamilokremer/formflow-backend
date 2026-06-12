package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.application.service.AuthEmailSender;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.ResendVerificationUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ResendVerificationCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the ResendVerificationUseCase input port.
 */
@Service
@RequiredArgsConstructor
public class ResendVerificationService implements ResendVerificationUseCase {

    private final UserRepositoryPort userRepository;
    private final TenantRepositoryPort tenantRepository;
    private final AuthEmailSender authEmailSender;

    @Override
    @Transactional
    public void execute(ResendVerificationCommand command) {
        User user = userRepository.findByIdAndTenantId(command.userId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.auth.unauthorized", HttpStatus.UNAUTHORIZED));

        if (user.isEmailVerified()) {
            throw new BusinessException("error.auth.email_already_verified", HttpStatus.BAD_REQUEST);
        }

        Tenant tenant = tenantRepository.findById(command.tenantId())
                .orElseThrow(() -> new BusinessException("error.auth.unauthorized", HttpStatus.UNAUTHORIZED));

        authEmailSender.sendEmailVerification(user, tenant);
    }
}
