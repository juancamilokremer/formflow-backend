package com.kodelabs.formflow.modules.auth.domain.port.out;

import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserInvitationRepositoryPort {

    UserInvitation save(UserInvitation invitation);

    Optional<UserInvitation> findByTokenHash(String tokenHash);

    Optional<UserInvitation> findByIdAndTenantId(UUID id, UUID tenantId);

    List<UserInvitation> findAllPendingByTenantId(UUID tenantId);

    boolean existsPendingByEmailAndTenantId(String email, UUID tenantId);
}
