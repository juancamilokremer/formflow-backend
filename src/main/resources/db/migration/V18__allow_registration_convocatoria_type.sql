ALTER TABLE convocatorias DROP CONSTRAINT chk_convocatorias_type;

ALTER TABLE convocatorias ADD CONSTRAINT chk_convocatorias_type CHECK (type IN ('CANDIDATES', 'DIAGNOSTIC', 'REGISTRATION'));
