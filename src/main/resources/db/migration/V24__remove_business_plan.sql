-- BUSINESS is merged into PRO (same enforced limits already, see PlanLimits) —
-- defensive no-op if no tenant ever had this plan, but costs nothing to run.
UPDATE tenants SET plan = 'PRO' WHERE plan = 'BUSINESS';

ALTER TABLE tenants DROP CONSTRAINT chk_tenant_plan;
ALTER TABLE tenants ADD CONSTRAINT chk_tenant_plan CHECK (plan IN ('FREE','STARTER','PRO','ENTERPRISE'));
