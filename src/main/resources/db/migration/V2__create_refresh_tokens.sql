-- ============================================================
-- V2: Tabla de refresh tokens (rotación de uso único)
-- FormFlow - Kode Labs - Issue #13
-- ============================================================

-- Se almacena solo el hash SHA-256 (hex, 64 chars) del token, nunca el valor en claro.
-- revoked_at IS NOT NULL → token rotado o invalidado (logout / password reset).
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tenant_id   UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
