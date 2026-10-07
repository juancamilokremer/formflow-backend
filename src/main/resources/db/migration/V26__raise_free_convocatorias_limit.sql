-- FREE's convocatorias_limit was 0 since plan_limits existed (V25, and the hardcoded
-- PlanLimits.forPlan() before it) — since every form is born inside a convocatoria/
-- encuesta (no standalone POST /forms), this made it impossible for any FREE tenant to
-- ever create a single form, contradicting the advertised "2 forms" and blocking the
-- onboarding wizard (frontend#8) for every new signup. Raised to match forms_limit (2).
UPDATE plan_limits SET convocatorias_limit = 2, updated_at = NOW() WHERE plan = 'FREE';
