-- ============================================================
-- V1: Tablas base de tenants y usuarios
-- FormFlow - Kode Labs
-- ============================================================

-- Tenants (empresas clientes)
CREATE TABLE tenants (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug             VARCHAR(100) NOT NULL UNIQUE,
    name             VARCHAR(150) NOT NULL,
    logo_url         VARCHAR(200),
    primary_color    VARCHAR(7),
    secondary_color  VARCHAR(7),
    plan             VARCHAR(20) NOT NULL DEFAULT 'FREE',
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_tenant_plan   CHECK (plan   IN ('FREE','STARTER','PRO','BUSINESS','ENTERPRISE')),
    CONSTRAINT chk_tenant_status CHECK (status IN ('ACTIVE','SUSPENDED','CANCELLED'))
);

-- Usuarios
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(20) NOT NULL DEFAULT 'VIEWER',
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_tenant_email UNIQUE (tenant_id, email),
    CONSTRAINT chk_user_role CHECK (role IN ('TENANT_ADMIN','EDITOR','VIEWER'))
);

-- Índices
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_tenants_slug ON tenants(slug);
