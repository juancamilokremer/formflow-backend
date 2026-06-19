CREATE TABLE form_responses (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    form_id           UUID        NOT NULL REFERENCES forms(id) ON DELETE RESTRICT,
    tenant_id         UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    convocatoria_id   UUID        NULL,
    respondent_token  UUID        NOT NULL,
    form_snapshot     JSONB       NOT NULL,
    submitted_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_form_responses_respondent_token UNIQUE (respondent_token)
);

CREATE INDEX idx_form_responses_form_id       ON form_responses(form_id);
CREATE INDEX idx_form_responses_tenant_id     ON form_responses(tenant_id);
CREATE INDEX idx_form_responses_convocatoria  ON form_responses(convocatoria_id) WHERE convocatoria_id IS NOT NULL;
