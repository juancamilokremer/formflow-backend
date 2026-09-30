package com.kodelabs.formflow.modules.auth.application.usecase.user;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Shared by ChangeUserRoleService and RevokeUserAccessService — both need the same
 * "a tenant can never end up with zero active TENANT_ADMIN" rule (backend#8's own
 * acceptance criteria), so it lives in one place instead of being duplicated twice.
 */
final class LastAdminGuard {

    private LastAdminGuard() {}

    /** Call BEFORE the change that would remove targetUser's admin status (role change away
     *  from TENANT_ADMIN, or revocation) actually happens. */
    static void assertAnotherAdminRemains(UserRepositoryPort userRepository, User targetUser) {
        if (targetUser.getRole() != UserRole.TENANT_ADMIN) return;

        long activeAdmins = userRepository.countByTenantIdAndRoleAndActiveTrue(
                targetUser.getTenantId(), UserRole.TENANT_ADMIN);
        if (activeAdmins <= 1) {
            throw new BusinessException("error.user.must_keep_one_admin", HttpStatus.BAD_REQUEST);
        }
    }
}
