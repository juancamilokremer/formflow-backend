CREATE TABLE convocatorias (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    form_id          UUID         NOT NULL REFERENCES forms(id) ON DELETE RESTRICT,
    name             VARCHAR(200) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    category_weights JSONB        NOT NULL DEFAULT '[]',
    scoring_config   JSONB        NOT NULL DEFAULT '{"aptoMin":70,"revisarMin":50}',
    start_date       TIMESTAMPTZ  NULL,
    end_date         TIMESTAMPTZ  NULL,
    deleted_at       TIMESTAMPTZ  NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_convocatorias_tenant_id ON convocatorias(tenant_id);
CREATE INDEX idx_convocatorias_form_id   ON convocatorias(form_id);

CREATE TABLE candidates (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    convocatoria_id UUID         NOT NULL REFERENCES convocatorias(id) ON DELETE CASCADE,
    tenant_id       UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name            VARCHAR(200) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    token           UUID         NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'INVITED',
    response_id     UUID         NULL REFERENCES form_responses(id) ON DELETE SET NULL,
    scores          JSONB        NULL,
    invited_at      TIMESTAMPTZ  NULL,
    responded_at    TIMESTAMPTZ  NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_candidates_token              UNIQUE (token),
    CONSTRAINT uq_candidates_convocatoria_email UNIQUE (convocatoria_id, email)
);

CREATE INDEX idx_candidates_convocatoria_id ON candidates(convocatoria_id);
CREATE INDEX idx_candidates_tenant_id       ON candidates(tenant_id);
CREATE INDEX idx_candidates_token           ON candidates(token);
