package com.kodelabs.formflow.modules.auth.domain.port.out;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port: persistence operations the domain needs for User.
 * The domain only knows this interface, never the JPA implementation.
 */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);

    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByEmailAndTenantId(String email, UUID tenantId);

    long countByTenantIdAndActiveTrue(UUID tenantId);

    /** Active (non-revoked) users of the tenant, for the admin listing (backend#8). */
    List<User> findAllByTenantIdAndActiveTrue(UUID tenantId);

    /** Used to enforce "at least one active TENANT_ADMIN must remain" (backend#8). */
    long countByTenantIdAndRoleAndActiveTrue(UUID tenantId, UserRole role);
}
