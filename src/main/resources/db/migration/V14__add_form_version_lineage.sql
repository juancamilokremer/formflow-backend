-- V14: Add version lineage to forms (#69)
-- previous_version_id: de qué formulario específico se clonó este (contexto/display).
-- root_form_id: raíz de toda la familia de versiones — compartido por todos los
-- formularios generados a partir del mismo original, sin importar cuántos pasos de
-- distancia. Permite calcular el siguiente número de versión mirando toda la familia
-- (MAX(version) + 1) en vez de solo el padre inmediato, evitando colisiones cuando
-- un mismo formulario bloqueado genera varias versiones independientes entre sí.
-- NULL en cualquiera de las dos columnas = el formulario es su propia raíz (v1 original).

ALTER TABLE forms
    ADD COLUMN previous_version_id UUID REFERENCES forms(id);

ALTER TABLE forms
    ADD COLUMN root_form_id UUID REFERENCES forms(id);

CREATE INDEX idx_forms_root_form_id ON forms(root_form_id) WHERE deleted_at IS NULL;
