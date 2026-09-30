-- ============================================================
-- V20: Nuevo rol SUPER_ADMIN (backend#5 - gestion de tenants)
-- ============================================================

ALTER TABLE users DROP CONSTRAINT chk_user_role;
ALTER TABLE users ADD CONSTRAINT chk_user_role
    CHECK (role IN ('TENANT_ADMIN','EDITOR','VIEWER','SUPER_ADMIN'));
