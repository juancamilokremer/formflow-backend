-- ============================================================
-- V21: Invitaciones de usuario (backend#8)
-- ============================================================

CREATE TABLE user_invitations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    email               VARCHAR(150) NOT NULL,
    role                VARCHAR(20) NOT NULL,
    token_hash          VARCHAR(64) NOT NULL UNIQUE,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    invited_by_user_id  UUID NOT NULL,
    expires_at          TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- SUPER_ADMIN is deliberately excluded: those are platform-level accounts set up
    -- directly by Kode Labs, never self-service through a tenant admin (see backend#5).
    CONSTRAINT chk_invitation_role   CHECK (role IN ('TENANT_ADMIN','EDITOR','VIEWER')),
    CONSTRAINT chk_invitation_status CHECK (status IN ('PENDING','ACCEPTED','CANCELLED'))
);

CREATE INDEX idx_user_invitations_tenant_id ON user_invitations(tenant_id);
