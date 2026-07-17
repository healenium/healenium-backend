package com.epam.healenium.tenant;

import com.epam.healenium.tenant.registry.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TenantValidationServiceTest {

    private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private TenantRepository tenantRepository;
    private TenantValidationService service;

    @BeforeEach
    void setUp() {
        tenantRepository = mock(TenantRepository.class);
        service = new TenantValidationService(tenantRepository, Duration.ofMinutes(5), 100);
    }

    @Test
    void cachesAllowedTenant() {
        when(tenantRepository.existsByIdAndStatus(TENANT_ID, "ACTIVE")).thenReturn(true);

        assertThat(service.isTenantAllowed(TENANT_ID)).isTrue();
        assertThat(service.isTenantAllowed(TENANT_ID)).isTrue();

        verify(tenantRepository, times(1)).existsByIdAndStatus(TENANT_ID, "ACTIVE");
    }

    @Test
    void cachesDeniedTenant() {
        when(tenantRepository.existsByIdAndStatus(TENANT_ID, "ACTIVE")).thenReturn(false);

        assertThat(service.isTenantAllowed(TENANT_ID)).isFalse();
        assertThat(service.isTenantAllowed(TENANT_ID)).isFalse();

        verify(tenantRepository, times(1)).existsByIdAndStatus(TENANT_ID, "ACTIVE");
    }

    @Test
    void invalidateForcesReload() {
        when(tenantRepository.existsByIdAndStatus(TENANT_ID, "ACTIVE")).thenReturn(true, false);

        assertThat(service.isTenantAllowed(TENANT_ID)).isTrue();
        service.invalidate(TENANT_ID);
        assertThat(service.isTenantAllowed(TENANT_ID)).isFalse();

        verify(tenantRepository, times(2)).existsByIdAndStatus(TENANT_ID, "ACTIVE");
    }
}
