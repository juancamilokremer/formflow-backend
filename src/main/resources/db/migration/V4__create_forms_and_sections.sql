-- ============================================================
-- V4: Formularios, secciones, preguntas y opciones de respuesta
-- FormFlow - Kode Labs
-- ============================================================

CREATE TABLE forms (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    type                VARCHAR(30) NOT NULL,
    version             INTEGER     NOT NULL DEFAULT 1,
    time_limit_seconds  INTEGER,
    deleted_at          TIMESTAMPTZ,
    created_by          UUID        REFERENCES users(id),
    updated_by          UUID        REFERENCES users(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_form_type CHECK (type IN ('CANDIDATES','DIAGNOSTIC','REGISTRATION'))
);

CREATE TABLE form_sections (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    form_id     UUID        NOT NULL REFERENCES forms(id) ON DELETE CASCADE,
    tenant_id   UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    position    INTEGER     NOT NULL DEFAULT 0,
    deleted_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- question types and scoring config are added in BE #2
CREATE TABLE form_questions (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    section_id          UUID        NOT NULL REFERENCES form_sections(id) ON DELETE CASCADE,
    form_id             UUID        NOT NULL REFERENCES forms(id) ON DELETE CASCADE,
    tenant_id           UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    title               VARCHAR(500) NOT NULL,
    description         TEXT,
    type                VARCHAR(30) NOT NULL,
    position            INTEGER     NOT NULL DEFAULT 0,
    required            BOOLEAN     NOT NULL DEFAULT FALSE,
    category_id         UUID,
    time_limit_seconds  INTEGER,
    config              JSONB       NOT NULL DEFAULT '{}',
    deleted_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_question_type CHECK (type IN ('TEXT','SINGLE','MULTIPLE','SCALE','DATE','FILE','MATRIX','NPS'))
);

CREATE INDEX idx_forms_tenant_id        ON forms(tenant_id)             WHERE deleted_at IS NULL;
CREATE INDEX idx_form_sections_form_id  ON form_sections(form_id)       WHERE deleted_at IS NULL;
CREATE INDEX idx_form_questions_section ON form_questions(section_id)   WHERE deleted_at IS NULL;
CREATE INDEX idx_form_questions_form    ON form_questions(form_id)      WHERE deleted_at IS NULL;
