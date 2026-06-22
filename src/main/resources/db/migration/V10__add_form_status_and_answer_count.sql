-- V10: Add status to forms table
-- status: DRAFT = form under construction, ACTIVE = published and accepting responses, ARCHIVED = closed

ALTER TABLE forms
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

ALTER TABLE forms
    ADD CONSTRAINT chk_form_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED'));

CREATE INDEX idx_forms_tenant_status ON forms(tenant_id, status) WHERE deleted_at IS NULL;
