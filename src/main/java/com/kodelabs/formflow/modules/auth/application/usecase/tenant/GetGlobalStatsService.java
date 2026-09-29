package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.domain.port.in.GetGlobalStatsUseCase;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.GlobalStatsResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantUsagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class GetGlobalStatsService implements GetGlobalStatsUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final TenantUsagePort tenantUsagePort;

    @Override
    public GlobalStatsResult execute() {
        YearMonth currentMonth = YearMonth.now(ZoneOffset.UTC);
        Instant monthStart = currentMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();

        long totalTenants = tenantRepository.countAll(null, null);
        long responsesThisMonth = tenantUsagePort.countAllResponsesThisMonth(monthStart, monthEnd);
        var tenantsByPlan = tenantRepository.countByPlan();

        return new GlobalStatsResult(totalTenants, responsesThisMonth, tenantsByPlan);
    }
}
