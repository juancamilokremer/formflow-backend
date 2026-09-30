package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.domain.model.PlanLimits;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.GetTenantUsageUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetTenantUsageQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantUsageResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantUsagePort;
import com.kodelabs.formflow.modules.auth.domain.port.out.UserRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class GetTenantUsageService implements GetTenantUsageUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final UserRepositoryPort userRepository;
    private final TenantUsagePort tenantUsagePort;

    @Override
    public TenantUsageResult execute(GetTenantUsageQuery query) {
        Tenant tenant = tenantRepository.findById(query.tenantId())
                .orElseThrow(() -> new BusinessException("error.tenant.not_found"));

        PlanLimits limits = PlanLimits.forPlan(tenant.getPlan());
        YearMonth currentMonth = YearMonth.now(ZoneOffset.UTC);
        Instant monthStart = currentMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();

        long formsUsed = tenantUsagePort.countForms(tenant.getId());
        long responsesThisMonth = tenantUsagePort.countResponsesThisMonth(tenant.getId(), monthStart, monthEnd);
        long usersCount = userRepository.countByTenantIdAndActiveTrue(tenant.getId());

        return new TenantUsageResult(
                tenant.getPlan(), formsUsed, limits.formsLimit(),
                responsesThisMonth, limits.responsesLimit(), usersCount, limits.usersLimit(),
                limits.canExportExcel());
    }
}
