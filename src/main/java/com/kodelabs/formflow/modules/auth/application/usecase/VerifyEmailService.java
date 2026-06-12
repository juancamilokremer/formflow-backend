package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.domain.model.EmailToken;
import com.kodelabs.formflow.modules.auth.domain.model.EmailTokenType;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.VerifyEmailUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.VerifyEmailCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.EmailTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the VerifyEmailUseCase input port.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyEmailService implements VerifyEmailUseCase {

    private static final String INVALID_TOKEN = "error.auth.email_token_invalid";

    private final EmailTokenRepositoryPort emailTokenRepository;
    private final UserRepositoryPort userRepository;
    private final TokenServicePort tokenService;

    @Override
    @Transactional
    public void execute(VerifyEmailCommand command) {
        EmailToken token = emailTokenRepository
                .findByTokenHashAndType(tokenService.hashToken(command.token()), EmailTokenType.EMAIL_VERIFICATION)
                .filter(EmailToken::isUsable)
                .orElseThrow(() -> new BusinessException(INVALID_TOKEN, HttpStatus.BAD_REQUEST));

        User user = userRepository.findByIdAndTenantId(token.getUserId(), token.getTenantId())
                .orElseThrow(() -> new BusinessException(INVALID_TOKEN, HttpStatus.BAD_REQUEST));

        user.setEmailVerified(true);
        userRepository.save(user);

        token.markUsed();
        emailTokenRepository.save(token);

        log.info("Email verified for user {}", user.getId());
    }
}
