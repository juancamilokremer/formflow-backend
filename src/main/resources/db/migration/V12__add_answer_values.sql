ALTER TABLE form_responses
    ADD COLUMN candidate_id UUID NULL REFERENCES candidates(id) ON DELETE SET NULL,
    ADD COLUMN started_at   TIMESTAMPTZ NULL;

CREATE INDEX idx_form_responses_candidate_id ON form_responses(candidate_id)
    WHERE candidate_id IS NOT NULL;

CREATE TABLE answer_values (
    id          UUID  PRIMARY KEY DEFAULT gen_random_uuid(),
    response_id UUID  NOT NULL REFERENCES form_responses(id) ON DELETE CASCADE,
    question_id UUID  NOT NULL,
    value       JSONB NOT NULL
);

CREATE INDEX idx_answer_values_response_id ON answer_values(response_id);
CREATE INDEX idx_answer_values_question_id ON answer_values(question_id);
