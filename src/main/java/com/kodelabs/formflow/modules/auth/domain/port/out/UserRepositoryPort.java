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

    /** Excludes the given role — used to keep SUPER_ADMIN out of a tenant's own usage
     *  count (#176). */
    long countByTenantIdAndActiveTrueAndRoleNot(UUID tenantId, UserRole role);

    /** Active (non-revoked) users of the tenant, for the admin listing (backend#8).
     *  Excludes the given role — SUPER_ADMIN is a platform account, not part of any
     *  tenant's own team (#176). */
    List<User> findAllByTenantIdAndActiveTrueAndRoleNot(UUID tenantId, UserRole role);

    /** Used to enforce "at least one active TENANT_ADMIN must remain" (backend#8). */
    long countByTenantIdAndRoleAndActiveTrue(UUID tenantId, UserRole role);
}
