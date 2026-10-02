package com.kodelabs.formflow.modules.auth.application.usecase.me;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.DeleteAvatarUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.DeleteAvatarCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.MeResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Only clears user.avatarUrl — does not delete the stored file, same trade-off already
 *  accepted for DeleteLogoService (#7): recovering the fileId from the URL would couple
 *  this to one specific FileStoragePort adapter's URL shape. */
@Service
@RequiredArgsConstructor
public class DeleteAvatarService implements DeleteAvatarUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    @Transactional
    public MeResult execute(DeleteAvatarCommand command) {
        User user = userRepository.findByIdAndTenantId(command.userId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.user.not_found", HttpStatus.NOT_FOUND));

        user.setAvatarUrl(null);
        return MeResult.from(userRepository.save(user));
    }
}
