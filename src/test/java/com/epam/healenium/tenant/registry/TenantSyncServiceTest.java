package com.epam.healenium.tenant.registry;

import com.epam.healenium.tenant.TenantValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantSyncServiceTest {

    private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantValidationService tenantValidationService;

    @InjectMocks
    private TenantSyncService tenantSyncService;

    @Test
    void upsertsNewTenantAndInvalidatesCache() {
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TenantSyncRequest request = new TenantSyncRequest();
        request.setId(TENANT_ID);
        request.setName("Acme");
        request.setStatus(TenantStatuses.ACTIVE);

        Tenant saved = tenantSyncService.upsert(request);

        assertThat(saved.getId()).isEqualTo(TENANT_ID);
        assertThat(saved.getName()).isEqualTo("Acme");
        assertThat(saved.getStatus()).isEqualTo(TenantStatuses.ACTIVE);

        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Acme");
        verify(tenantValidationService).invalidate(TENANT_ID);
    }

    @Test
    void updatesExistingTenantStatus() {
        Tenant existing = new Tenant().setId(TENANT_ID).setName("Acme").setStatus(TenantStatuses.ACTIVE);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(existing));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TenantSyncRequest request = new TenantSyncRequest();
        request.setId(TENANT_ID);
        request.setName("Acme");
        request.setStatus("DISABLED");

        Tenant saved = tenantSyncService.upsert(request);

        assertThat(saved.getStatus()).isEqualTo(TenantStatuses.DISABLED);
        verify(tenantValidationService).invalidate(TENANT_ID);
    }

    @Test
    void upsertAllAppliesEachRequest() {
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TenantSyncRequest request = new TenantSyncRequest();
        request.setId(TENANT_ID);
        request.setName("Acme");
        request.setStatus(TenantStatuses.ACTIVE);

        List<Tenant> saved = tenantSyncService.upsertAll(List.of(request));

        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getName()).isEqualTo("Acme");
        verify(tenantValidationService).invalidate(TENANT_ID);
    }
}
