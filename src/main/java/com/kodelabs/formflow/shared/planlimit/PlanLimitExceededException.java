package com.kodelabs.formflow.shared.planlimit;

import com.kodelabs.formflow.modules.auth.domain.model.TenantPlan;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/** Thrown when a tenant tries to exceed its plan's usage limit (backend#6). */
public class PlanLimitExceededException extends BusinessException {

    public PlanLimitExceededException(String messageKey, int limit, TenantPlan currentPlan, TenantPlan suggestedPlan) {
        super(messageKey, HttpStatus.PAYMENT_REQUIRED, limit, currentPlan, suggestedPlan);
    }
}
