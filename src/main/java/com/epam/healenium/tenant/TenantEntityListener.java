package com.epam.healenium.tenant;

import jakarta.persistence.PrePersist;

import java.util.UUID;

/**
 * Sets {@code tenant_id} on insert.
 * Uses {@link TenantContext#getTenantId()} or falls back to {@link TenantFilter#DEFAULT_TENANT_ID}.
 */
public class TenantEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (!(entity instanceof TenantAwareEntity tenantAware)) {
            return;
        }
        if (tenantAware.getTenantId() != null) {
            return;
        }

        UUID contextTenantId = TenantContext.getTenantId();
        if (contextTenantId != null) {
            tenantAware.setTenantId(contextTenantId);
            return;
        }

        tenantAware.setTenantId(TenantFilter.DEFAULT_TENANT_ID);
    }
}
