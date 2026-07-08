package com.epam.healenium.tenant;

import jakarta.persistence.PrePersist;

import java.util.UUID;

/**
 * Pro edition listener: tenant must be explicitly set via TenantContext.
 */
public class ProTenantEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (!(entity instanceof TenantAwareEntity tenantAware)) {
            return;
        }

        UUID tenantId = tenantAware.getTenantId();
        if (tenantId != null) {
            return;
        }

        UUID ctxTenantId = TenantContext.getTenantId();
        if (ctxTenantId == null) {
            throw new IllegalStateException("Tenant is not set in TenantContext for entity: " + entity.getClass().getName());
        }

        tenantAware.setTenantId(ctxTenantId);
    }
}
