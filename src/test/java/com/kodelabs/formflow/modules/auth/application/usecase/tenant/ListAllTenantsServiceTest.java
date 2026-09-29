package com.kodelabs.formflow.modules.auth.application.usecase.tenant;

import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.ListTenantsQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.TenantPageResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAllTenantsServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @InjectMocks private ListAllTenantsService service;

    @Test
    void computesTotalPagesFromTheTotalCount() {
        var query = new ListTenantsQuery(0, 20, null, null);
        when(tenantRepository.countAll(null, null)).thenReturn(45L);
        when(tenantRepository.findAll(0, 20, null, null)).thenReturn(
                List.of(Tenant.builder().slug("a").build(), Tenant.builder().slug("b").build()));

        TenantPageResult result = service.execute(query);

        assertThat(result.totalElements()).isEqualTo(45);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.items()).hasSize(2);
    }
}
