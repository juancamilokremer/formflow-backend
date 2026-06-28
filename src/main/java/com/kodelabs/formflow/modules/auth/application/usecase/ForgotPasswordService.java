package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.application.service.AuthEmailSender;
import com.kodelabs.formflow.modules.auth.domain.port.in.ForgotPasswordUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ForgotPasswordCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the ForgotPasswordUseCase input port.
 *
 * NEVER throws for an unknown tenant/email: the HTTP response is the same
 * 200 either way (user enumeration protection). The real outcome is logged.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordService implements ForgotPasswordUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final UserRepositoryPort userRepository;
    private final AuthEmailSender authEmailSender;

    @Override
    @Transactional
    public void execute(ForgotPasswordCommand command) {
        String email = command.email().toLowerCase().trim();

        tenantRepository.findBySlug(command.tenantSlug()).ifPresentOrElse(
                tenant -> userRepository.findByEmailAndTenantId(email, tenant.getId()).ifPresentOrElse(
                        authEmailSender::sendPasswordReset,
                        () -> log.info("Password reset requested for unknown email '{}' on tenant '{}'",
                                email, command.tenantSlug())),
                () -> log.info("Password reset requested for unknown tenant '{}'", command.tenantSlug()));
    }
}
