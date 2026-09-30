package com.kodelabs.formflow.modules.auth.infrastructure.persistence.adapter;

import com.kodelabs.formflow.modules.auth.domain.model.InvitationStatus;
import com.kodelabs.formflow.modules.auth.domain.model.UserInvitation;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserInvitationRepositoryPort;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.mapper.UserInvitationPersistenceMapper;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository.UserInvitationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserInvitationRepositoryAdapter implements UserInvitationRepositoryPort {

    private final UserInvitationJpaRepository jpaRepository;
    private final UserInvitationPersistenceMapper mapper;

    @Override
    public UserInvitation save(UserInvitation invitation) {
        return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(invitation)));
    }

    @Override
    public Optional<UserInvitation> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(mapper::toDomain);
    }

    @Override
    public Optional<UserInvitation> findByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.findByIdAndTenantId(id, tenantId).map(mapper::toDomain);
    }

    @Override
    public List<UserInvitation> findAllPendingByTenantId(UUID tenantId) {
        // "Pending and not expired" — expiry is computed (isUsable), not a stored status,
        // so the not-expired half of the filter happens here rather than in SQL.
        return jpaRepository.findAllByTenantIdAndStatus(tenantId, InvitationStatus.PENDING).stream()
                .map(mapper::toDomain)
                .filter(UserInvitation::isUsable)
                .toList();
    }

    @Override
    public boolean existsPendingByEmailAndTenantId(String email, UUID tenantId) {
        return jpaRepository.existsByEmailAndTenantIdAndStatus(email, tenantId, InvitationStatus.PENDING);
    }
}
