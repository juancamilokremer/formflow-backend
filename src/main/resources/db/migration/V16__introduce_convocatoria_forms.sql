CREATE TABLE convocatoria_forms (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    convocatoria_id  UUID         NOT NULL REFERENCES convocatorias(id) ON DELETE CASCADE,
    form_id          UUID         NOT NULL REFERENCES forms(id) ON DELETE RESTRICT,
    weight           INTEGER      NOT NULL DEFAULT 100,
    category_weights JSONB        NOT NULL DEFAULT '[]',
    min_score        INTEGER      NULL,
    position         INTEGER      NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_convocatoria_forms_convocatoria_id UNIQUE (convocatoria_id)
);

CREATE INDEX idx_convocatoria_forms_convocatoria_id ON convocatoria_forms(convocatoria_id);
CREATE INDEX idx_convocatoria_forms_form_id         ON convocatoria_forms(form_id);

INSERT INTO convocatoria_forms (convocatoria_id, form_id, weight, category_weights, position, created_at, updated_at)
SELECT id, form_id, 100, category_weights, 0, created_at, updated_at
FROM convocatorias
WHERE form_id IS NOT NULL;

ALTER TABLE convocatorias DROP COLUMN form_id;
ALTER TABLE convocatorias DROP COLUMN category_weights;
