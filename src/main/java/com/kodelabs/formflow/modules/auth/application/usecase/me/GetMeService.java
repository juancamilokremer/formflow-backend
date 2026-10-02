package com.kodelabs.formflow.modules.auth.application.usecase.me;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetMeUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetMeQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.MeResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMeService implements GetMeUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    public MeResult execute(GetMeQuery query) {
        User user = userRepository.findByIdAndTenantId(query.userId(), query.tenantId())
                .orElseThrow(() -> new BusinessException("error.user.not_found", HttpStatus.NOT_FOUND));
        return MeResult.from(user);
    }
}
