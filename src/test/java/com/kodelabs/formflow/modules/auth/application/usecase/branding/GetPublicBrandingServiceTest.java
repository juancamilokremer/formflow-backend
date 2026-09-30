package com.kodelabs.formflow.modules.auth.application.usecase.branding;

import com.kodelabs.formflow.modules.auth.application.service.BrandingCache;
import com.kodelabs.formflow.modules.auth.domain.model.Tenant;
import com.kodelabs.formflow.modules.auth.domain.port.in.command.GetPublicBrandingQuery;
import com.kodelabs.formflow.modules.auth.domain.port.in.result.BrandingResult;
import com.kodelabs.formflow.modules.auth.domain.port.out.TenantRepositoryPort;
import com.kodelabs.formflow.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPublicBrandingServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private BrandingCache brandingCache;
    @InjectMocks private GetPublicBrandingService service;

    @Test
    void loadsFromTheRepositoryAndCachesOnAMiss() {
        Tenant tenant = Tenant.builder().slug("empresa-abc").name("Empresa ABC").build();
        when(brandingCache.get("empresa-abc")).thenReturn(Optional.empty());
        when(tenantRepository.findBySlug("empresa-abc")).thenReturn(Optional.of(tenant));

        BrandingResult result = service.execute(new GetPublicBrandingQuery("empresa-abc"));

        assertThat(result.tenantName()).isEqualTo("Empresa ABC");
        verify(brandingCache).put("empresa-abc", result);
    }

    @Test
    void returnsTheCachedValueWithoutHittingTheRepository() {
        BrandingResult cached = new BrandingResult("Empresa ABC", null, null, null, null);
        when(brandingCache.get("empresa-abc")).thenReturn(Optional.of(cached));

        BrandingResult result = service.execute(new GetPublicBrandingQuery("empresa-abc"));

        assertThat(result).isSameAs(cached);
        verify(tenantRepository, never()).findBySlug(anyString());
    }

    @Test
    void throwsNotFoundWhenTheSlugDoesNotExist() {
        when(brandingCache.get("no-existe")).thenReturn(Optional.empty());
        when(tenantRepository.findBySlug("no-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetPublicBrandingQuery("no-existe")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
