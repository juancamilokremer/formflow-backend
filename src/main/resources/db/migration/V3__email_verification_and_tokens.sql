-- ============================================================
-- V3: Email verification flag + single-use email tokens
-- FormFlow - Kode Labs - Issue #25
-- ============================================================

ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Only the SHA-256 hash (hex, 64 chars) of the token is stored, never the
-- raw value sent by email. used_at IS NOT NULL -> already consumed.
CREATE TABLE email_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tenant_id   UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    type        VARCHAR(20) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_email_token_type CHECK (type IN ('EMAIL_VERIFICATION','PASSWORD_RESET'))
);

CREATE INDEX idx_email_tokens_user_id ON email_tokens(user_id);
