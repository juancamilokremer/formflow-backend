ALTER TABLE convocatorias ALTER COLUMN form_id DROP NOT NULL;

ALTER TABLE convocatorias ADD COLUMN type VARCHAR(20);

UPDATE convocatorias c SET type = f.type FROM forms f WHERE c.form_id = f.id;

ALTER TABLE convocatorias ALTER COLUMN type SET NOT NULL;

ALTER TABLE convocatorias ADD CONSTRAINT chk_convocatorias_type CHECK (type IN ('CANDIDATES', 'DIAGNOSTIC'));
