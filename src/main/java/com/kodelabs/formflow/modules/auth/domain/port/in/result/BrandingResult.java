package com.kodelabs.formflow.modules.auth.domain.port.in.result;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;

/** Deliberately excludes everything not safe to expose publicly (id, slug, plan, status). */
public record BrandingResult(
        String tenantName,
        String logoUrl,
        String primaryColor,
        String secondaryColor,
        String faviconUrl
) {
    /** faviconUrl is always null — there is no favicon upload flow yet (not part of #7). */
    public static BrandingResult from(Tenant tenant) {
        return new BrandingResult(
                tenant.getName(), tenant.getLogoUrl(), tenant.getPrimaryColor(),
                tenant.getSecondaryColor(), null);
    }
}
