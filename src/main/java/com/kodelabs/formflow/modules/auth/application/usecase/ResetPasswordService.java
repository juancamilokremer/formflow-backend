package com.kodelabs.formflow.modules.auth.application.usecase;

import com.kodelabs.formflow.modules.auth.domain.model.EmailToken;
import com.kodelabs.formflow.modules.auth.domain.model.EmailTokenType;
import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.ResetPasswordUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ResetPasswordCommand;
import com.kodelabs.formflow.modules.auth.domain.port.out.EmailTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.PasswordHasherPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.RefreshTokenRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TokenServicePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the ResetPasswordUseCase input port.
 *
 * The token is single-use, and every active refresh token of the user is
 * revoked: if the password was reset because of a compromise, any stolen
 * session dies with it.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

    private static final String INVALID_TOKEN = "error.auth.email_token_invalid";

    private final EmailTokenRepositoryPort emailTokenRepository;
    private final UserRepositoryPort userRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenServicePort tokenService;

    @Override
    @Transactional
    public void execute(ResetPasswordCommand command) {
        EmailToken token = findUsableToken(command.token());
        User user = loadUser(token);

        user.setPasswordHash(passwordHasher.hash(command.newPassword()));
        userRepository.save(user);

        token.markUsed();
        emailTokenRepository.save(token);

        // Compromised-session safety: every active refresh token dies with the old password
        refreshTokenRepository.revokeAllByUserId(user.getId());

        log.info("Password reset completed for user {} — all refresh tokens revoked", user.getId());
    }

    private EmailToken findUsableToken(String rawToken) {
        return emailTokenRepository
                .findByTokenHashAndType(tokenService.hashToken(rawToken), EmailTokenType.PASSWORD_RESET)
                .filter(EmailToken::isUsable)
                .orElseThrow(() -> new BusinessException(INVALID_TOKEN, HttpStatus.BAD_REQUEST));
    }

    private User loadUser(EmailToken token) {
        return userRepository.findByIdAndTenantId(token.getUserId(), token.getTenantId())
                .filter(User::isActive)
                .orElseThrow(() -> new BusinessException(INVALID_TOKEN, HttpStatus.BAD_REQUEST));
    }
}
