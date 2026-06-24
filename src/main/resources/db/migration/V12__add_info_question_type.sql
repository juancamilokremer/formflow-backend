ALTER TABLE form_questions DROP CONSTRAINT chk_question_type;
ALTER TABLE form_questions ADD CONSTRAINT chk_question_type
    CHECK (type IN ('text','single','multiple','scale','date','file','matrix','nps','info'));
