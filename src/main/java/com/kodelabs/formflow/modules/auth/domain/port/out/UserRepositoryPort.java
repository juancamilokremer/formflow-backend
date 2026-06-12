package com.kodelabs.formflow.modules.auth.domain.port.out;

import com.kodelabs.formflow.modules.auth.domain.model.User;

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
}
