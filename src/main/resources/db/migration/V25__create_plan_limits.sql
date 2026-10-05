CREATE TABLE plan_limits (
    plan                VARCHAR(20)  PRIMARY KEY CHECK (plan IN ('FREE','STARTER','PRO','ENTERPRISE')),
    forms_limit         INT          NOT NULL,
    responses_limit     INT          NOT NULL,
    users_limit         INT          NOT NULL,
    convocatorias_limit INT          NOT NULL,
    can_export_excel    BOOLEAN      NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Seed = exactly today's hardcoded values (PlanLimits.java before this migration) —
-- this deploy makes them editable, it does not change anyone's behavior.
INSERT INTO plan_limits (plan, forms_limit, responses_limit, users_limit, convocatorias_limit, can_export_excel) VALUES
    ('FREE', 2, 50, 1, 0, false),
    ('STARTER', 10, 500, 3, 5, true),
    ('PRO', -1, -1, -1, -1, true),
    ('ENTERPRISE', -1, -1, -1, -1, true);
