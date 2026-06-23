ALTER TABLE form_questions DROP CONSTRAINT chk_question_type;
UPDATE form_questions SET type = LOWER(type) WHERE type != LOWER(type);
ALTER TABLE form_questions ADD CONSTRAINT chk_question_type
    CHECK (type IN ('text','single','multiple','scale','date','file','matrix','nps'));
