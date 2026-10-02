package com.kodelabs.formflow.modules.auth.application.usecase.me;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.UpdateMeUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.UpdateMeCommand;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.MeResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateMeService implements UpdateMeUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    @Transactional
    public MeResult execute(UpdateMeCommand command) {
        User user = userRepository.findByIdAndTenantId(command.userId(), command.tenantId())
                .orElseThrow(() -> new BusinessException("error.user.not_found", HttpStatus.NOT_FOUND));

        user.setFirstName(command.firstName());
        user.setLastName(command.lastName());

        return MeResult.from(userRepository.save(user));
    }
}
