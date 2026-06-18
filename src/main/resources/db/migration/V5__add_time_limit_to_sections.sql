-- ============================================================
-- V5: Add time_limit_seconds to form_sections
-- FormFlow - Kode Labs
-- ============================================================

ALTER TABLE form_sections ADD COLUMN time_limit_seconds INTEGER NULL;
