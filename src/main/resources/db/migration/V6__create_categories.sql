-- ============================================================
-- V6: Tabla de categorías para agrupación de preguntas y scoring
-- FormFlow - Kode Labs
-- ============================================================

CREATE TABLE categories (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    color       VARCHAR(7)  NOT NULL DEFAULT '#6B7280',
    description VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_category_name_per_tenant UNIQUE (tenant_id, name)
);

CREATE INDEX idx_categories_tenant_id ON categories(tenant_id);
